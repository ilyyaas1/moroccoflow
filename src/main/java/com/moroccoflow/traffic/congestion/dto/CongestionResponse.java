package com.moroccoflow.traffic.congestion.dto;

import com.moroccoflow.traffic.congestion.CongestionLevel;

import java.math.BigDecimal;
import java.time.Instant;

public record CongestionResponse(
        Long roadId,
        Long cityId,
        Instant measuredAt,
        BigDecimal congestionIndex,
        CongestionLevel level,
        BigDecimal averageSpeed,
        BigDecimal occupancyRate,
        Integer speedLimitKm
) {
}
