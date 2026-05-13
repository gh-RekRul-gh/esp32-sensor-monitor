package com.vkr.sensor_monitor.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "analysis")
@Getter
@Setter
public class AnalysisConfig {

    private int windowSize = 30;
    private double zscoreThreshold = 3.5;
    private double driftSlopeThreshold = 0.11;

    // Per-metric overrides; fallback to global threshold if not set
    private Map<String, Double> zscoreThresholds = new HashMap<>();
    private Map<String, Double> driftSlopeThresholds = new HashMap<>();

    public double getZscoreThresholdFor(String metric) {
        return zscoreThresholds.getOrDefault(metric, zscoreThreshold);
    }

    public double getDriftSlopeThresholdFor(String metric) {
        return driftSlopeThresholds.getOrDefault(metric, driftSlopeThreshold);
    }
}
