package com.vkr.sensor_monitor.repository;

import com.vkr.sensor_monitor.entity.Dht22Reading;
import com.vkr.sensor_monitor.entity.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface Dht22ReadingRepository extends JpaRepository<Dht22Reading, Long> {

    @Query("SELECT r FROM Dht22Reading r WHERE r.sensor = :sensor ORDER BY r.timestamp DESC LIMIT :limit")
    List<Dht22Reading> findLatestBySensor(@Param("sensor") Sensor sensor, @Param("limit") int limit);
}
