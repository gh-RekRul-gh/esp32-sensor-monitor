package com.vkr.sensor_monitor.repository;

import com.vkr.sensor_monitor.entity.SensorAnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorAnalysisResultRepository extends JpaRepository<SensorAnalysisResult, Long> {
}
