package com.moroccoflow.traffic.congestion;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@ConfigurationProperties(prefix = "moroccoflow.congestion")
public class CongestionProperties {

    /**
     * Weight for (1 - speed/limit). Combined with occupancy-weight and normalized to sum to 1.
     */
    private double speedWeight = 0.6;

    /**
     * Weight for occupancy/100.
     */
    private double occupancyWeight = 0.4;

    /** Inclusive lower bound for MODERATE. */
    private double moderateThreshold = 0.30;

    /** Inclusive lower bound for HIGH. */
    private double highThreshold = 0.60;

    /** Inclusive lower bound for CRITICAL. */
    private double criticalThreshold = 0.80;

    public double speedWeight() {
        return speedWeight;
    }

    public double occupancyWeight() {
        return occupancyWeight;
    }

    public double moderateThreshold() {
        return moderateThreshold;
    }

    public double highThreshold() {
        return highThreshold;
    }

    public double criticalThreshold() {
        return criticalThreshold;
    }

    public double normalizedSpeedWeight() {
        double sum = speedWeight + occupancyWeight;
        if (sum <= 0) {
            return 0.5;
        }
        return speedWeight / sum;
    }

    public double normalizedOccupancyWeight() {
        return 1.0 - normalizedSpeedWeight();
    }
}
