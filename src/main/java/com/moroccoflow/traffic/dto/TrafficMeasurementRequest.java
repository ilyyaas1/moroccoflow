package com.moroccoflow.traffic.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record TrafficMeasurementRequest(
        @NotNull(message = "Road ID is required")
        Long roadId,

        @NotNull(message = "Timestamp is required")
        Instant timestamp,

        @NotNull(message = "Vehicle count is required")
        @Min(value = 0, message = "Vehicle count must be at least 0")
        Integer vehicleCount,

        @NotNull(message = "Average speed is required")
        @DecimalMin(value = "0.0", message = "Average speed must be at least 0")
        BigDecimal averageSpeed,

        @NotNull(message = "Occupancy rate is required")
        @DecimalMin(value = "0.0", message = "Occupancy rate must be between 0 and 100")
        @DecimalMax(value = "100.0", message = "Occupancy rate must be between 0 and 100")
        BigDecimal occupancyRate,

        @NotNull(message = "Travel time is required")
        @DecimalMin(value = "0.0", message = "Travel time must be at least 0")
        BigDecimal travelTime
) {
}
