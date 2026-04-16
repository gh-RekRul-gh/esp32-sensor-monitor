package com.vkr.sensor_monitor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mq2_reading")
@Getter
@Setter
@NoArgsConstructor
public class Mq2Reading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private Integer rawValue;
    private Boolean gasDetected;

    public Mq2Reading(Sensor sensor, Integer rawValue, Boolean gasDetected) {
        this.sensor = sensor;
        this.timestamp = LocalDateTime.now();
        this.rawValue = rawValue;
        this.gasDetected = gasDetected;
    }
}
