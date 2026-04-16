package com.vkr.sensor_monitor.repository;

import com.vkr.sensor_monitor.entity.Bmp280Reading;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Bmp280ReadingRepository extends JpaRepository<Bmp280Reading, Long> {
}
