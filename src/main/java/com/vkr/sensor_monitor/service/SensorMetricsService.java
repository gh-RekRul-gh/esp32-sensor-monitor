package com.vkr.sensor_monitor.service;

import java.util.function.Supplier;

import com.vkr.sensor_monitor.entity.Bmp280Reading;
import com.vkr.sensor_monitor.entity.SensorStatus;
import com.vkr.sensor_monitor.repository.Bmp280ReadingRepository;
import com.vkr.sensor_monitor.repository.Dht22ReadingRepository;
import com.vkr.sensor_monitor.repository.SensorRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class SensorMetricsService {

    private final MeterRegistry meterRegistry;
    private final SensorRepository sensorRepo;
    private final Bmp280ReadingRepository bmp280Repo;

    public SensorMetricsService(MeterRegistry meterRegistry,
                                SensorRepository sensorRepo,
                                Bmp280ReadingRepository bmp280Repo,
                                Dht22ReadingRepository dht22Repo) {
        this.meterRegistry = meterRegistry;
        this.sensorRepo = sensorRepo;
        this.bmp280Repo = bmp280Repo;

        registerGauges();
    }

    private void registerGauges() {
        Gauge.builder("sensors.online.count", () -> sensorRepo.countByStatus(SensorStatus.ONLINE))
                .description("Number of online sensors")
                .register(meterRegistry);

        Gauge.builder("sensors.offline.count", () -> sensorRepo.countByStatus(SensorStatus.OFFLINE))
                .description("Number of offline sensors")
                .register(meterRegistry);

        // Средние показания
        Gauge.builder("sensors.temperature.average", this::getAvgBmp280Temp)
                .tag("type", "BMP280")
                .description("Average temperature from BMP280 sensors")
                .register(meterRegistry);
    }

    private Double getAvgBmp280Temp() {
        return bmp280Repo.findLatestReadings(10)
                .stream()
                .mapToDouble(Bmp280Reading::getTemperature)
                .average()
                .orElse(0.0);
    }
}
