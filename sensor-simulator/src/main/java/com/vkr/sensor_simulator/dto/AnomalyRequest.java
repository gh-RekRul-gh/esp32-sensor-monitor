package com.vkr.sensor_simulator.dto;

public record AnomalyRequest(
        Dht22Anomaly dht22,
        Bmp280Anomaly bmp280,
        Mq2Anomaly mq2,
        Hcsr04Anomaly hcsr04,
        Integer decaySeconds
) {
    public record Dht22Anomaly(Float temperature, Float humidity) {}
    public record Bmp280Anomaly(Float temperature, Float pressureHpa) {}
    public record Mq2Anomaly(Integer rawValue) {}
    public record Hcsr04Anomaly(Integer distanceCm) {}
}
