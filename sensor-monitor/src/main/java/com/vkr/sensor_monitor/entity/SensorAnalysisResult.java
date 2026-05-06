package com.vkr.sensor_monitor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_analysis_result", indexes = {
        @Index(name = "idx_analysis_sensor_timestamp", columnList = "sensor_id, timestamp DESC"),
        @Index(name = "idx_analysis_detected", columnList = "sensor_id, detected")
})
@Getter
@Setter
@NoArgsConstructor
public class SensorAnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "analysis_type")
    private AnalysisType analysisType;

    @Column(nullable = false)
    private String metric;

    @Column(nullable = false)
    private boolean detected;

    @Column(name = "current_value")
    private Double currentValue;

    private Double mean;

    @Column(name = "std_dev")
    private Double stdDev;

    @Column(name = "z_score")
    private Double zScore;

    private Double slope;

    private String details;

    public SensorAnalysisResult(Sensor sensor, AnalysisType analysisType, String metric,
                                boolean detected, Double currentValue, Double mean, Double stdDev,
                                Double zScore, Double slope, String details) {
        this.sensor = sensor;
        this.timestamp = LocalDateTime.now();
        this.analysisType = analysisType;
        this.metric = metric;
        this.detected = detected;
        this.currentValue = currentValue;
        this.mean = mean;
        this.stdDev = stdDev;
        this.zScore = zScore;
        this.slope = slope;
        this.details = details;
    }
}
