package com.vkr.sensor_monitor.repository;

import java.util.List;

import com.vkr.sensor_monitor.entity.Bmp280Reading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface Bmp280ReadingRepository extends JpaRepository<Bmp280Reading, Long> {

    @Query("SELECT b FROM Bmp280Reading b ORDER BY b.timestamp DESC LIMIT :limit")
    List<Bmp280Reading> findLatestReadings(@Param("limit") Integer limit);
}
