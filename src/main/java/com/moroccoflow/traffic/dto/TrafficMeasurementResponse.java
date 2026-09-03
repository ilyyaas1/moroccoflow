package com.moroccoflow.traffic.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TrafficMeasurementResponse(
        Long id,
        Long roadId,
        Long cityId,
        Instant timestamp,
        Integer vehicleCount,
        BigDecimal averageSpeed,
        BigDecimal occupancyRate,
        BigDecimal travelTime,
        Instant createdAt
) {
}
