package com.vkr.sensor_monitor.controller;

import com.vkr.sensor_monitor.dto.SensorReadingRequest;
import com.vkr.sensor_monitor.service.SensorReadingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sensor")
@RequiredArgsConstructor
@Log4j2
public class SensorReadingController {

    private final SensorReadingService sensorReadingService;

    @PostMapping("/readings")
    public ResponseEntity<Void> saveReadings(@RequestBody @Valid SensorReadingRequest request) {
        log.info("Received sensor reading request: {}", request);
        sensorReadingService.processReading(request);
        return ResponseEntity.ok().build();
    }
}
