package com.vkr.sensor_monitor.controller;

import com.vkr.sensor_monitor.dto.SensorReadingRequest;
import com.vkr.sensor_monitor.service.SensorReadingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sensor")
@RequiredArgsConstructor
public class SensorReadingController {

    private final SensorReadingService sensorReadingService;

    @PostMapping("/readings")
    public ResponseEntity<Void> saveReadings(@RequestBody @Valid SensorReadingRequest request) {
        sensorReadingService.processReading(request);
        return ResponseEntity.ok().build();
    }
}
