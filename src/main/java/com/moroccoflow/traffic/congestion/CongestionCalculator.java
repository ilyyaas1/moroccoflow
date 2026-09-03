package com.moroccoflow.traffic.congestion;

import com.moroccoflow.traffic.congestion.dto.CongestionResponse;
import com.moroccoflow.traffic.dto.TrafficMeasurementResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Deterministic congestion from latest speed vs limit and occupancy.
 * Index = speedWeight * (1 - speed/limit) + occupancyWeight * (occupancy/100), then clamped to 0–1.
 * Levels: below moderate = LOW; then MODERATE, HIGH, CRITICAL at the configured thresholds.
 */
@Component
public class CongestionCalculator {

    private final CongestionProperties properties;

    public CongestionCalculator(CongestionProperties properties) {
        this.properties = properties;
        if (properties.moderateThreshold() >= properties.highThreshold()
                || properties.highThreshold() >= properties.criticalThreshold()) {
            throw new IllegalArgumentException(
                    "Congestion thresholds must satisfy moderate < high < critical");
        }
    }

    public CongestionResponse calculate(TrafficMeasurementResponse measurement, int speedLimitKm) {
        double speed = measurement.averageSpeed().doubleValue();
        double occupancy = measurement.occupancyRate().doubleValue();
        double limit = Math.max(speedLimitKm, 1);

        double speedFactor = Math.clamp(1.0 - (speed / limit), 0.0, 1.0);
        double occupancyFactor = Math.clamp(occupancy / 100.0, 0.0, 1.0);
        double index = Math.clamp(
                properties.normalizedSpeedWeight() * speedFactor
                        + properties.normalizedOccupancyWeight() * occupancyFactor,
                0.0,
                1.0);

        return new CongestionResponse(
                measurement.roadId(),
                measurement.cityId(),
                measurement.timestamp(),
                round(index),
                toLevel(index),
                measurement.averageSpeed(),
                measurement.occupancyRate(),
                speedLimitKm
        );
    }

    CongestionLevel toLevel(double index) {
        if (index >= properties.criticalThreshold()) {
            return CongestionLevel.CRITICAL;
        }
        if (index >= properties.highThreshold()) {
            return CongestionLevel.HIGH;
        }
        if (index >= properties.moderateThreshold()) {
            return CongestionLevel.MODERATE;
        }
        return CongestionLevel.LOW;
    }

    private static BigDecimal round(double index) {
        return BigDecimal.valueOf(index).setScale(4, RoundingMode.HALF_UP);
    }
}
