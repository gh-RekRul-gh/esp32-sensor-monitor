package com.vkr.sensor_simulator.service;

import com.vkr.sensor_simulator.dto.AnomalyRequest;
import com.vkr.sensor_simulator.dto.SensorReadingRequest;
import com.vkr.sensor_simulator.dto.StateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class SensorStateService {

    private static final double THETA = 0.1;
    private static final double DT = 0.5;
    private static final int GAS_THRESHOLD = 2000;

    private final Random random = new Random();
    private volatile String deviceId = "esp32";

    private final SensorField dht22Temp   = new SensorField(23.0, 0.15, 19.0, 28.0);
    private final SensorField dht22Hum    = new SensorField(50.0, 0.30, 35.0, 70.0);
    private final SensorField bmp280Temp  = new SensorField(24.0, 0.05, 21.0, 27.0);
    private final SensorField bmp280Press = new SensorField(1013.0, 0.20, 1005.0, 1020.0);
    private final SensorField mq2Raw      = new SensorField(380.0, 5.0, 280.0, 520.0);
    private final SensorField hcsr04Dist  = new SensorField(42.0, 0.30, 30.0, 55.0);

    @Scheduled(fixedDelay = 500)
    public synchronized void tick() {
        dht22Temp.tick(random);
        dht22Hum.tick(random);
        bmp280Temp.tick(random);
        bmp280Press.tick(random);
        mq2Raw.tick(random);
        hcsr04Dist.tick(random);
    }

    public synchronized SensorReadingRequest getCurrentReading() {
        boolean gasDetected = mq2Raw.get() > GAS_THRESHOLD;
        return new SensorReadingRequest(
                deviceId,
                new SensorReadingRequest.Dht22Data(round1(dht22Temp.get()), round1(dht22Hum.get())),
                new SensorReadingRequest.Bmp280Data(round2(bmp280Temp.get()), round1(bmp280Press.get())),
                new SensorReadingRequest.Mq2Data((int) Math.round(mq2Raw.get()), gasDetected),
                new SensorReadingRequest.Hcsr04Data((int) Math.round(hcsr04Dist.get()))
        );
    }

    public synchronized void injectAnomaly(AnomalyRequest req) {
        int decay = req.decaySeconds() != null ? req.decaySeconds() : 7;
        if (req.dht22() != null) {
            if (req.dht22().temperature() != null) dht22Temp.inject(req.dht22().temperature(), decay);
            if (req.dht22().humidity() != null)    dht22Hum.inject(req.dht22().humidity(), decay);
        }
        if (req.bmp280() != null) {
            if (req.bmp280().temperature() != null)  bmp280Temp.inject(req.bmp280().temperature(), decay);
            if (req.bmp280().pressureHpa() != null)   bmp280Press.inject(req.bmp280().pressureHpa(), decay);
        }
        if (req.mq2() != null && req.mq2().rawValue() != null) {
            mq2Raw.inject(req.mq2().rawValue(), decay);
        }
        if (req.hcsr04() != null && req.hcsr04().distanceCm() != null) {
            hcsr04Dist.inject(req.hcsr04().distanceCm(), decay);
        }
        log.debug("Anomaly injected, decay={}s", decay);
    }

    public synchronized void setDeviceId(String newId) {
        this.deviceId = newId;
        log.info("Device ID changed to {}", newId);
    }

    public synchronized StateResponse getState() {
        List<StateResponse.ActiveAnomaly> anomalies = new ArrayList<>();
        addIfActive(anomalies, "dht22.temperature",   dht22Temp);
        addIfActive(anomalies, "dht22.humidity",      dht22Hum);
        addIfActive(anomalies, "bmp280.temperature",  bmp280Temp);
        addIfActive(anomalies, "bmp280.pressureHpa",  bmp280Press);
        addIfActive(anomalies, "mq2.rawValue",        mq2Raw);
        addIfActive(anomalies, "hcsr04.distanceCm",   hcsr04Dist);

        return new StateResponse(
                deviceId,
                new StateResponse.SensorValues(
                        round1(dht22Temp.get()),  round1(dht22Hum.get()),
                        round2(bmp280Temp.get()), round1(bmp280Press.get()),
                        (int) Math.round(mq2Raw.get()), mq2Raw.get() > GAS_THRESHOLD,
                        (int) Math.round(hcsr04Dist.get())
                ),
                anomalies
        );
    }

    private void addIfActive(List<StateResponse.ActiveAnomaly> list, String name, SensorField field) {
        long remaining = field.remainingDecaySeconds();
        if (remaining > 0) {
            list.add(new StateResponse.ActiveAnomaly(name, field.get(), remaining));
        }
    }

    private static float round1(double v) {
        return Math.round(v * 10f) / 10f;
    }

    private static float round2(double v) {
        return Math.round(v * 100f) / 100f;
    }

    private static class SensorField {
        double value;
        final double mean;
        final double sigma;
        final double min;
        final double max;

        Double overrideFrom;
        Instant overrideStart;
        Instant overrideEnd;

        SensorField(double mean, double sigma, double min, double max) {
            this.value = mean;
            this.mean  = mean;
            this.sigma = sigma;
            this.min   = min;
            this.max   = max;
        }

        void tick(Random rng) {
            if (overrideEnd != null) {
                Instant now = Instant.now();
                if (now.isBefore(overrideEnd)) {
                    double total   = Duration.between(overrideStart, overrideEnd).toMillis();
                    double elapsed = Duration.between(overrideStart, now).toMillis();
                    double progress = Math.min(elapsed / total, 1.0);
                    value = overrideFrom + (mean - overrideFrom) * progress;
                } else {
                    overrideEnd = null;
                    overrideFrom = null;
                    value = mean;
                }
            } else {
                double delta = THETA * (mean - value) * DT + sigma * rng.nextGaussian();
                value = Math.max(min, Math.min(max, value + delta));
            }
        }

        void inject(double anomalyValue, int decaySeconds) {
            overrideFrom  = anomalyValue;
            overrideStart = Instant.now();
            overrideEnd   = overrideStart.plusSeconds(decaySeconds);
            value         = anomalyValue;
        }

        double get() { return value; }

        long remainingDecaySeconds() {
            if (overrideEnd == null) return 0;
            return Math.max(0, Duration.between(Instant.now(), overrideEnd).getSeconds());
        }
    }
}
