package com.vkr.sensor_simulator.dto;

public record SensorReadingRequest(
        String deviceId,
        Dht22Data dht22,
        Bmp280Data bmp280,
        Mq2Data mq2,
        Hcsr04Data hcsr04
) {
    public record Dht22Data(float temperature, float humidity) {}
    public record Bmp280Data(float temperature, float pressureHpa) {}
    public record Mq2Data(int rawValue, boolean gasDetected) {}
    public record Hcsr04Data(int distanceCm) {}
}
