package com.vkr.sensor_monitor.scheduler;

import com.vkr.sensor_monitor.entity.Sensor;
import com.vkr.sensor_monitor.entity.SensorStatus;
import com.vkr.sensor_monitor.repository.SensorRepository;
import com.vkr.sensor_monitor.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OfflineCheckScheduler {

    private final SensorRepository sensorRepository;
    private final AlertService alertService;

    @Value("${alert.thresholds.offline-minutes:5}")
    private int offlineMinutes;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void checkOfflineSensors() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(offlineMinutes);
        List<Sensor> sensors = sensorRepository.findAll();

        for (Sensor sensor : sensors) {
            if (sensor.getStatus() == SensorStatus.ONLINE
                    && sensor.getLastSeenAt() != null
                    && sensor.getLastSeenAt().isBefore(threshold)) {

                sensor.setStatus(SensorStatus.OFFLINE);
                sensorRepository.save(sensor);
                log.warn("Sensor {} marked as OFFLINE", sensor.getName());

                alertService.sendIfCooldownPassed(
                        "offline-" + sensor.getName(),
                        "Sensor offline: " + sensor.getName(),
                        "Датчик %s не выходил на связь с %s (более %d мин).%nПоследнее местонахождение: %s"
                                .formatted(sensor.getName(), sensor.getLastSeenAt(), offlineMinutes, sensor.getLocation())
                );
            }
        }
    }
}
