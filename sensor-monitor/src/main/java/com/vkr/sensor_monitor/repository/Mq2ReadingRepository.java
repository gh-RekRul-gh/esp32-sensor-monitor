package com.vkr.sensor_monitor.repository;

import com.vkr.sensor_monitor.entity.Mq2Reading;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Mq2ReadingRepository extends JpaRepository<Mq2Reading, Long> {
}
