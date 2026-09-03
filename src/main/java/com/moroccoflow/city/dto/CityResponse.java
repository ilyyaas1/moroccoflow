package com.moroccoflow.city.dto;


import java.math.BigDecimal;
import java.time.Instant;

public record CityResponse(
        Long id,
        String name,
        String country,
        BigDecimal latitude,
        BigDecimal longitude,
        Instant createdAt,
        Instant updatedAt
) {
}