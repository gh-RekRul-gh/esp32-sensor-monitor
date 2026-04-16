package com.vkr.sensor_monitor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "bmp280_reading")
@Getter
@Setter
@NoArgsConstructor
public class Bmp280Reading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private Float temperature;

    @Column(name = "pressure_hpa")
    private Float pressureHpa;

    public Bmp280Reading(Sensor sensor, Float temperature, Float pressureHpa) {
        this.sensor = sensor;
        this.timestamp = LocalDateTime.now();
        this.temperature = temperature;
        this.pressureHpa = pressureHpa;
    }
}
