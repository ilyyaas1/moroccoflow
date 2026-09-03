package com.moroccoflow.road.dto;


import com.moroccoflow.road.entity.RoadEntity.RoadType;
import jakarta.validation.constraints.*;
import java.util.Map;

public record RoadRequest(
        @NotNull(message = "City ID is required")
        Long cityId,

        @NotBlank(message = "Road name is required")
        @Size(max = 150, message = "Road name must be at most 150 characters")
        String name,

        @NotNull(message = "Road type is required")
        RoadType roadType,

        @NotNull(message = "Speed limit is required")
        @Min(value = 5, message = "Speed limit must be at least 5 km/h")
        @Max(value = 130, message = "Speed limit must be at most 130 km/h")
        Integer speedLimitKm,

        @Min(value = 1, message = "Road must have at least 1 lane")
        Integer lanes,

        Map<String, Object> geometry
) {
}