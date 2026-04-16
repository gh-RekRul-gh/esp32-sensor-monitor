package com.vkr.sensor_monitor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "sensor")
@Getter
@Setter
@NoArgsConstructor
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SensorType type;

    private String location;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SensorStatus status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastSeenAt;

    public Sensor(String name, SensorType type, String location) {
        this.name = name;
        this.type = type;
        this.location = location;
        this.status = SensorStatus.ONLINE;
    }
}
