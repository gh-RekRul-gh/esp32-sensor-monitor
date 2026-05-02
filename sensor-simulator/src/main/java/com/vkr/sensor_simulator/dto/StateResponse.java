package com.vkr.sensor_simulator.dto;

import java.util.List;

public record StateResponse(
        String deviceId,
        boolean enabled,
        SensorValues current,
        List<ActiveAnomaly> activeAnomalies
) {
    public record SensorValues(
            float dht22Temperature,
            float dht22Humidity,
            float bmp280Temperature,
            float bmp280PressureHpa,
            int mq2RawValue,
            boolean mq2GasDetected,
            int hcsr04DistanceCm
    ) {}

    public record ActiveAnomaly(String field, double currentValue, long remainingSeconds) {}
}
