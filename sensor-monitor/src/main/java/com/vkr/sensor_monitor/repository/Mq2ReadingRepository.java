package com.vkr.sensor_monitor.repository;

import com.vkr.sensor_monitor.entity.Mq2Reading;
import com.vkr.sensor_monitor.entity.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface Mq2ReadingRepository extends JpaRepository<Mq2Reading, Long> {

    @Query("SELECT r FROM Mq2Reading r WHERE r.sensor = :sensor ORDER BY r.timestamp DESC LIMIT :limit")
    List<Mq2Reading> findLatestBySensor(@Param("sensor") Sensor sensor, @Param("limit") int limit);
}
