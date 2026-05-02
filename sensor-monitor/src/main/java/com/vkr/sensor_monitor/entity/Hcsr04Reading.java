package com.vkr.sensor_monitor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "hcsr04_reading")
@Getter
@Setter
@NoArgsConstructor
public class Hcsr04Reading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "distance_cm")
    private Integer distanceCm;

    public Hcsr04Reading(Sensor sensor, Integer distanceCm) {
        this.sensor = sensor;
        this.timestamp = LocalDateTime.now();
        this.distanceCm = distanceCm;
    }
}
