package com.moroccoflow.intersection.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record IntersectionRequest(
        @NotNull(message = "City ID is required")
        Long cityId,

        @NotBlank(message = "Intersection name is required")
        @Size(max = 150, message = "Name must be at most 150 characters")
        String name,

        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90", message = "Latitude must be between -90 and 90")
        @DecimalMax(value = "90", message = "Latitude must be between -90 and 90")
        BigDecimal latitude,

        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180", message = "Longitude must be between -180 and 180")
        @DecimalMax(value = "180", message = "Longitude must be between -180 and 180")
        BigDecimal longitude
) {
}
