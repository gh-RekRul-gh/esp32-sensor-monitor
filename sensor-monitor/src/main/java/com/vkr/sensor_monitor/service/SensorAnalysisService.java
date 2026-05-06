package com.vkr.sensor_monitor.service;

import com.vkr.sensor_monitor.config.AnalysisConfig;
import com.vkr.sensor_monitor.entity.*;
import com.vkr.sensor_monitor.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorAnalysisService {

    private final SensorRepository sensorRepository;
    private final SensorAnalysisResultRepository analysisResultRepo;
    private final Dht22ReadingRepository dht22Repo;
    private final Bmp280ReadingRepository bmp280Repo;
    private final Mq2ReadingRepository mq2Repo;
    private final Hcsr04ReadingRepository hcsr04Repo;
    private final AlertService alertService;
    private final AnalysisConfig config;

    @Transactional
    public void analyzeDht22(Sensor sensor, Float currentTemp, Float currentHumidity) {
        List<Dht22Reading> history = dht22Repo.findLatestBySensor(sensor, config.getWindowSize() - 1);
        if (history.size() < config.getWindowSize() - 1) return;

        SensorStatus worst = SensorStatus.ONLINE;

        if (currentTemp != null) {
            List<Double> temps = buildWindow(currentTemp.doubleValue(),
                    history.stream().map(r -> r.getTemperature() != null ? r.getTemperature().doubleValue() : null).collect(Collectors.toList()));
            if (temps != null) {
                worst = worse(worst, runZScoreAnalysis(sensor, "temperature", temps));
                worst = worse(worst, runDriftAnalysis(sensor, "temperature", temps));
            }
        }

        if (currentHumidity != null) {
            List<Double> humids = buildWindow(currentHumidity.doubleValue(),
                    history.stream().map(r -> r.getHumidity() != null ? r.getHumidity().doubleValue() : null).collect(Collectors.toList()));
            if (humids != null) {
                worst = worse(worst, runZScoreAnalysis(sensor, "humidity", humids));
                worst = worse(worst, runDriftAnalysis(sensor, "humidity", humids));
            }
        }

        applyStatus(sensor, worst);
    }

    @Transactional
    public void analyzeBmp280(Sensor sensor, Float currentTemp, Float currentPressure) {
        List<Bmp280Reading> history = bmp280Repo.findLatestBySensor(sensor, config.getWindowSize() - 1);
        if (history.size() < config.getWindowSize() - 1) return;

        SensorStatus worst = SensorStatus.ONLINE;

        if (currentTemp != null) {
            List<Double> temps = buildWindow(currentTemp.doubleValue(),
                    history.stream().map(r -> r.getTemperature() != null ? r.getTemperature().doubleValue() : null).collect(Collectors.toList()));
            if (temps != null) {
                worst = worse(worst, runZScoreAnalysis(sensor, "temperature", temps));
                worst = worse(worst, runDriftAnalysis(sensor, "temperature", temps));
            }
        }

        if (currentPressure != null) {
            List<Double> pressures = buildWindow(currentPressure.doubleValue(),
                    history.stream().map(r -> r.getPressureHpa() != null ? r.getPressureHpa().doubleValue() : null).collect(Collectors.toList()));
            if (pressures != null) {
                worst = worse(worst, runZScoreAnalysis(sensor, "pressure_hpa", pressures));
                worst = worse(worst, runDriftAnalysis(sensor, "pressure_hpa", pressures));
            }
        }

        applyStatus(sensor, worst);
    }

    @Transactional
    public void analyzeMq2(Sensor sensor, Integer currentRawValue) {
        if (currentRawValue == null) return;
        List<Mq2Reading> history = mq2Repo.findLatestBySensor(sensor, config.getWindowSize() - 1);
        if (history.size() < config.getWindowSize() - 1) return;

        List<Double> rawValues = buildWindow(currentRawValue.doubleValue(),
                history.stream().map(r -> r.getRawValue() != null ? r.getRawValue().doubleValue() : null).collect(Collectors.toList()));
        if (rawValues == null) return;

        SensorStatus worst = SensorStatus.ONLINE;
        worst = worse(worst, runZScoreAnalysis(sensor, "raw_value", rawValues));
        worst = worse(worst, runDriftAnalysis(sensor, "raw_value", rawValues));

        applyStatus(sensor, worst);
    }

    @Transactional
    public void analyzeHcsr04(Sensor sensor, Integer currentDistance) {
        if (currentDistance == null) return;
        List<Hcsr04Reading> history = hcsr04Repo.findLatestBySensor(sensor, config.getWindowSize() - 1);
        if (history.size() < config.getWindowSize() - 1) return;

        List<Double> distances = buildWindow(currentDistance.doubleValue(),
                history.stream().map(r -> r.getDistanceCm() != null ? r.getDistanceCm().doubleValue() : null).collect(Collectors.toList()));
        if (distances == null) return;

        SensorStatus worst = SensorStatus.ONLINE;
        worst = worse(worst, runZScoreAnalysis(sensor, "distance_cm", distances));
        worst = worse(worst, runDriftAnalysis(sensor, "distance_cm", distances));

        applyStatus(sensor, worst);
    }

    // --- Core algorithms ---

    private SensorStatus runZScoreAnalysis(Sensor sensor, String metric, List<Double> valuesNewestFirst) {
        double current = valuesNewestFirst.getFirst();
        List<Double> window = valuesNewestFirst.subList(1, valuesNewestFirst.size());

        double mean = mean(window);
        double std = std(window, mean);

        if (std < 1e-9) {
            saveResult(sensor, AnalysisType.ANOMALY_ZSCORE, metric, false,
                    current, mean, std, 0.0, null, "Constant signal, analysis skipped");
            return SensorStatus.ONLINE;
        }

        double threshold = config.getZscoreThresholdFor(metric);
        double zScore = Math.abs(current - mean) / std;
        boolean detected = zScore > threshold;

        String details = "Z=%.3f (threshold=%.1f), mean=%.3f, std=%.3f".formatted(zScore, threshold, mean, std);
        saveResult(sensor, AnalysisType.ANOMALY_ZSCORE, metric, detected, current, mean, std, zScore, null, details);

        if (detected) {
            log.debug("Anomaly detected on sensor={} metric={} Z={}", sensor.getName(), metric, zScore);
            alertService.sendIfCooldownPassed(
                    "anomaly-" + metric + "-" + sensor.getName(),
                    "Anomaly detected: " + sensor.getName(),
                    "Датчик %s (%s): аномальное значение %.3f (Z=%.2f, среднее=%.3f, σ=%.3f)"
                            .formatted(sensor.getName(), metric, current, zScore, mean, std)
            );
            return SensorStatus.ANOMALY;
        }
        return SensorStatus.ONLINE;
    }

    private SensorStatus runDriftAnalysis(Sensor sensor, String metric, List<Double> valuesNewestFirst) {
        List<Double> chronological = new ArrayList<>(valuesNewestFirst);
        Collections.reverse(chronological);

        double mean = mean(chronological);
        double std = std(chronological, mean);
        double slope = computeLinearSlope(chronological);

        double normalizedSlope = (std < 1e-9) ? 0.0 : slope / std;
        double threshold = config.getDriftSlopeThresholdFor(metric);
        boolean detected = Math.abs(normalizedSlope) > threshold;

        String details = "slope=%.5f, norm_slope=%.4f (threshold=%.2f), std=%.3f"
                .formatted(slope, normalizedSlope, threshold, std);
        saveResult(sensor, AnalysisType.DRIFT_LINEAR, metric, detected,
                chronological.getLast(), mean, std, null, normalizedSlope, details);

        if (detected) {
            log.debug("Drift detected on sensor={} metric={} normSlope={}", sensor.getName(), metric, normalizedSlope);
            alertService.sendIfCooldownPassed(
                    "drift-" + metric + "-" + sensor.getName(),
                    "Sensor drift detected: " + sensor.getName(),
                    "Датчик %s (%s): обнаружен дрейф. Наклон тренда: %.5f (норм.: %.4f, порог: %.2f)"
                            .formatted(sensor.getName(), metric, slope, normalizedSlope, threshold)
            );
            return SensorStatus.DRIFT_DETECTED;
        }
        return SensorStatus.ONLINE;
    }

    // --- Math helpers ---

    private double mean(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private double std(List<Double> values, double mean) {
        double variance = values.stream()
                .mapToDouble(v -> (v - mean) * (v - mean))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }

    private double computeLinearSlope(List<Double> y) {
        int n = y.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += y.get(i);
            sumXY += (double) i * y.get(i);
            sumX2 += (double) i * i;
        }
        double denom = n * sumX2 - sumX * sumX;
        if (Math.abs(denom) < 1e-9) return 0.0;
        return (n * sumXY - sumX * sumY) / denom;
    }

    // --- Utils ---

    private List<Double> buildWindow(double current, List<Double> history) {
        if (history.stream().anyMatch(v -> v == null)) return null;
        List<Double> window = new ArrayList<>(history.size() + 1);
        window.add(current);
        window.addAll(history);
        return window;
    }

    private void saveResult(Sensor sensor, AnalysisType type, String metric, boolean detected,
                            Double currentValue, Double mean, Double stdDev,
                            Double zScore, Double slope, String details) {
        analysisResultRepo.save(new SensorAnalysisResult(
                sensor, type, metric, detected, currentValue, mean, stdDev, zScore, slope, details));
    }

    private SensorStatus worse(SensorStatus a, SensorStatus b) {
        if (a == SensorStatus.ANOMALY || b == SensorStatus.ANOMALY) return SensorStatus.ANOMALY;
        if (a == SensorStatus.DRIFT_DETECTED || b == SensorStatus.DRIFT_DETECTED) return SensorStatus.DRIFT_DETECTED;
        return SensorStatus.ONLINE;
    }

    private void applyStatus(Sensor sensor, SensorStatus newStatus) {
        if (newStatus != SensorStatus.ONLINE && sensor.getStatus() != newStatus) {
            sensor.setStatus(newStatus);
            sensorRepository.save(sensor);
        }
    }
}
