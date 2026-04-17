package com.vkr.sensor_monitor.service;

import com.vkr.sensor_monitor.entity.SensorStatus;
import com.vkr.sensor_monitor.repository.SensorRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class SensorMetricsService {

    private final MeterRegistry meterRegistry;
    private final SensorRepository sensorRepo;

    public SensorMetricsService(MeterRegistry meterRegistry, SensorRepository sensorRepo) {
        this.meterRegistry = meterRegistry;
        this.sensorRepo = sensorRepo;
        registerGauges();
    }

    private void registerGauges() {
        Gauge.builder("sensors.online.count", () -> sensorRepo.countByStatus(SensorStatus.ONLINE))
                .description("Number of online sensors")
                .register(meterRegistry);

        Gauge.builder("sensors.offline.count", () -> sensorRepo.countByStatus(SensorStatus.OFFLINE))
                .description("Number of offline sensors")
                .register(meterRegistry);
    }
}
