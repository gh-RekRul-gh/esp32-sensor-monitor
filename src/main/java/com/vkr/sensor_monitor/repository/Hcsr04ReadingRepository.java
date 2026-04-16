package com.vkr.sensor_monitor.repository;

import com.vkr.sensor_monitor.entity.Hcsr04Reading;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Hcsr04ReadingRepository extends JpaRepository<Hcsr04Reading, Long> {
}
