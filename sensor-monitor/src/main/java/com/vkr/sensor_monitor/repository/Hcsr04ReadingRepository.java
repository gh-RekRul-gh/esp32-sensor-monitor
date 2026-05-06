package com.vkr.sensor_monitor.repository;

import com.vkr.sensor_monitor.entity.Hcsr04Reading;
import com.vkr.sensor_monitor.entity.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface Hcsr04ReadingRepository extends JpaRepository<Hcsr04Reading, Long> {

    @Query("SELECT r FROM Hcsr04Reading r WHERE r.sensor = :sensor ORDER BY r.timestamp DESC LIMIT :limit")
    List<Hcsr04Reading> findLatestBySensor(@Param("sensor") Sensor sensor, @Param("limit") int limit);
}
