package com.vkr.sensor_monitor.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;

public record SensorReadingRequest(

        @NotBlank
        String deviceId,

        @Nullable Dht22Data dht22,
        @Nullable Bmp280Data bmp280,
        @Nullable Mq2Data mq2,
        @Nullable Hcsr04Data hcsr04

) {
    public record Dht22Data(Float temperature, Float humidity) {}
    public record Bmp280Data(Float temperature, Float pressureHpa) {}
    public record Mq2Data(Integer rawValue, Boolean gasDetected) {}
    public record Hcsr04Data(Integer distanceCm) {}
}
