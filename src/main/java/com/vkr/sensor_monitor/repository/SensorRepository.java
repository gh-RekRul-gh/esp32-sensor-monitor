package com.vkr.sensor_monitor.repository;

import com.vkr.sensor_monitor.entity.Sensor;
import com.vkr.sensor_monitor.entity.SensorStatus;
import com.vkr.sensor_monitor.entity.SensorType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SensorRepository extends JpaRepository<Sensor, Long> {
    Optional<Sensor> findByNameAndType(String name, SensorType type);

    int countByStatus(SensorStatus status);
}
