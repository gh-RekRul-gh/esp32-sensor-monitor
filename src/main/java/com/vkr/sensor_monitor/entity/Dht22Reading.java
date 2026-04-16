package com.vkr.sensor_monitor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "dht22_reading")
@Getter
@Setter
@NoArgsConstructor
public class Dht22Reading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private Float temperature;
    private Float humidity;

    public Dht22Reading(Sensor sensor, Float temperature, Float humidity) {
        this.sensor = sensor;
        this.timestamp = LocalDateTime.now();
        this.temperature = temperature;
        this.humidity = humidity;
    }
}
