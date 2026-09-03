package com.moroccoflow.road.dto;


import com.moroccoflow.road.entity.RoadEntity.RoadType;

import java.time.Instant;
import java.util.Map;

public record RoadResponse(
        Long id,
        Long cityId,
        String name,
        RoadType roadType,
        Integer speedLimitKm,
        Integer lanes,
        Map<String, Object> geometry,
        Instant createdAt,
        Instant updatedAt
) {
}
