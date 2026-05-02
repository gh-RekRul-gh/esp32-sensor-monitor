package com.vkr.sensor_simulator.controller;

import com.vkr.sensor_simulator.dto.AnomalyRequest;
import com.vkr.sensor_simulator.dto.DeviceIdRequest;
import com.vkr.sensor_simulator.dto.EnableRequest;
import com.vkr.sensor_simulator.dto.StateResponse;
import com.vkr.sensor_simulator.service.SensorStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/simulator")
@RequiredArgsConstructor
public class SimulatorController {

    private final SensorStateService stateService;

    @PostMapping("/anomaly")
    public ResponseEntity<Void> injectAnomaly(@RequestBody AnomalyRequest request) {
        stateService.injectAnomaly(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/device")
    public ResponseEntity<Void> setDeviceId(@RequestBody DeviceIdRequest request) {
        stateService.setDeviceId(request.newDeviceId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/enabled")
    public ResponseEntity<Void> setEnabled(@RequestBody EnableRequest request) {
        stateService.setEnabled(request.enabled());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/state")
    public ResponseEntity<StateResponse> getState() {
        return ResponseEntity.ok(stateService.getState());
    }
}
