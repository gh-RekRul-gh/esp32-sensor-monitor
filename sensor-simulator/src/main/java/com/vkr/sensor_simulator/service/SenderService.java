package com.vkr.sensor_simulator.service;

import com.vkr.sensor_simulator.dto.SensorReadingRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class SenderService {

    private final RestClient restClient;
    private final SensorStateService stateService;

    @Scheduled(fixedDelay = 2000)
    public void send() {
        if (!stateService.isEnabled()) return;
        SensorReadingRequest reading = stateService.getCurrentReading();
        try {
            restClient.post()
                    .body(reading)
                    .retrieve()
                    .toBodilessEntity();
            log.debug("Sent reading for device {}", reading.deviceId());
        } catch (Exception e) {
            log.warn("Failed to send reading: {}", e.getMessage());
        }
    }
}
