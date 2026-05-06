package com.vkr.sensor_monitor.service;

import com.vkr.sensor_monitor.dto.SensorReadingRequest;
import com.vkr.sensor_monitor.entity.*;
import com.vkr.sensor_monitor.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorReadingService {

    private final SensorRepository sensorRepository;
    private final Dht22ReadingRepository dht22Repo;
    private final Bmp280ReadingRepository bmp280Repo;
    private final Mq2ReadingRepository mq2Repo;
    private final Hcsr04ReadingRepository hcsr04Repo;
    private final AlertService alertService;
    private final SensorAnalysisService analysisService;

    @Value("${alert.thresholds.temperature-max:35.0}")
    private double temperatureMax;

    @Value("${alert.thresholds.humidity-max:80.0}")
    private double humidityMax;

    @Value("${alert.thresholds.pressure-min:950.0}")
    private double pressureMin;

    @Value("${alert.thresholds.pressure-max:1050.0}")
    private double pressureMax;

    @Value("${alert.thresholds.distance-min:10.0}")
    private double distanceMin;

    @Transactional
    public void processReading(SensorReadingRequest request) {
        if (request.dht22() != null) {
            Sensor sensor = findOrCreate(request.deviceId(), SensorType.DHT22);
            SensorReadingRequest.Dht22Data data = request.dht22();
            dht22Repo.save(new Dht22Reading(sensor, data.temperature(), data.humidity()));
            updateLastSeen(sensor);
            checkDht22Alerts(sensor.getName(), data);
            analysisService.analyzeDht22(sensor, data.temperature(), data.humidity());
        }

        if (request.bmp280() != null) {
            Sensor sensor = findOrCreate(request.deviceId(), SensorType.BMP280);
            SensorReadingRequest.Bmp280Data data = request.bmp280();
            bmp280Repo.save(new Bmp280Reading(sensor, data.temperature(), data.pressureHpa()));
            updateLastSeen(sensor);
            checkBmp280Alerts(sensor.getName(), data);
            analysisService.analyzeBmp280(sensor, data.temperature(), data.pressureHpa());
        }

        if (request.mq2() != null) {
            Sensor sensor = findOrCreate(request.deviceId(), SensorType.MQ2);
            SensorReadingRequest.Mq2Data data = request.mq2();
            mq2Repo.save(new Mq2Reading(sensor, data.rawValue(), data.gasDetected()));
            updateLastSeen(sensor);
            checkMq2Alerts(sensor.getName(), data);
            analysisService.analyzeMq2(sensor, data.rawValue());
        }

        if (request.hcsr04() != null) {
            Sensor sensor = findOrCreate(request.deviceId(), SensorType.HCSR04);
            SensorReadingRequest.Hcsr04Data data = request.hcsr04();
            hcsr04Repo.save(new Hcsr04Reading(sensor, data.distanceCm()));
            updateLastSeen(sensor);
            checkHcsr04Alerts(sensor.getName(), data);
            analysisService.analyzeHcsr04(sensor, data.distanceCm());
        }
    }

    private void checkDht22Alerts(String sensorName, SensorReadingRequest.Dht22Data data) {
        if (data.temperature() != null && data.temperature() > temperatureMax) {
            var messageBody = "Датчик %s зафиксировал высокую температуру: %.1f°C (порог: %.1f°C)"
                    .formatted(sensorName, data.temperature(), temperatureMax);
            log.info(messageBody);
            alertService.sendIfCooldownPassed(
                    "temp-high-" + sensorName,
                    "High temperature: " + sensorName,
                    messageBody
            );
        }
        if (data.humidity() != null && data.humidity() > humidityMax) {
            var messageBody = "Датчик %s зафиксировал высокую влажность: %.1f%% (порог: %.1f%%)"
                    .formatted(sensorName, data.humidity(), humidityMax);
            log.info(messageBody);
            alertService.sendIfCooldownPassed(
                    "hum-high-" + sensorName,
                    "High humidity: " + sensorName,
                    messageBody
            );
        }
    }

    private void checkMq2Alerts(String sensorName, SensorReadingRequest.Mq2Data data) {
        if (Boolean.TRUE.equals(data.gasDetected())) {
            var messageBody = "Датчик %s обнаружил утечку газа! Raw value: %d"
                    .formatted(sensorName, data.rawValue());
            log.info(messageBody);
            alertService.sendIfCooldownPassed(
                    "gas-detected-" + sensorName,
                    "Gas detected: " + sensorName,
                    messageBody
            );
        }
    }

    private void checkBmp280Alerts(String sensorName, SensorReadingRequest.Bmp280Data data) {
        if (data.pressureHpa() != null && data.pressureHpa() > pressureMax) {
            var messageBody = "Датчик %s зафиксировал высокое давление: %.1f гПа (порог: %.1f гПа)"
                    .formatted(sensorName, data.pressureHpa(), pressureMax);
            log.info(messageBody);
            alertService.sendIfCooldownPassed(
                    "pressure-high-" + sensorName,
                    "High pressure: " + sensorName,
                    messageBody
            );
        }
        if (data.pressureHpa() != null && data.pressureHpa() < pressureMin) {
            var messageBody = "Датчик %s зафиксировал низкое давление: %.1f гПа (порог: %.1f гПа)"
                    .formatted(sensorName, data.pressureHpa(), pressureMin);
            log.info(messageBody);
            alertService.sendIfCooldownPassed(
                    "pressure-low-" + sensorName,
                    "Low pressure: " + sensorName,
                    messageBody
            );
        }
    }

    private void checkHcsr04Alerts(String sensorName, SensorReadingRequest.Hcsr04Data data) {
        if (data.distanceCm() != null && data.distanceCm() < distanceMin) {
            var messageBody = "Датчик %s зафиксировал критически малое расстояние: %d см (порог: %.1f см) — уровень жидкости слишком высок"
                    .formatted(sensorName, data.distanceCm(), distanceMin);
            log.info(messageBody);
            alertService.sendIfCooldownPassed(
                    "distance-low-" + sensorName,
                    "High liquid level: " + sensorName,
                    messageBody
            );
        }
    }

    private Sensor findOrCreate(String deviceId, SensorType type) {
        String name = deviceId + "-" + type.name();
        return sensorRepository.findByNameAndType(name, type)
                .orElseGet(() -> sensorRepository.save(new Sensor(name, type, "ESP32")));
    }

    private void updateLastSeen(Sensor sensor) {
        boolean wasOffline = sensor.getStatus() == SensorStatus.OFFLINE;
        sensor.setLastSeenAt(LocalDateTime.now());
        sensor.setStatus(SensorStatus.ONLINE);
        sensorRepository.save(sensor);

        if (wasOffline) {
            log.info("Sensor {} recovered (was OFFLINE)", sensor.getName());
            alertService.send(
                    "Sensor recovered: " + sensor.getName(),
                    "Датчик %s снова в сети.".formatted(sensor.getName())
            );
        }
    }
}
