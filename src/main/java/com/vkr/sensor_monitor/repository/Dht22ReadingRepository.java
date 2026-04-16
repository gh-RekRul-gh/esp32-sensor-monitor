package com.vkr.sensor_monitor.repository;

import com.vkr.sensor_monitor.entity.Dht22Reading;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Dht22ReadingRepository extends JpaRepository<Dht22Reading, Long> {
}
