package com.moroccoflow.intersection.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record IntersectionResponse(
        Long id,
        Long cityId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        Instant createdAt,
        Instant updatedAt
) {
}