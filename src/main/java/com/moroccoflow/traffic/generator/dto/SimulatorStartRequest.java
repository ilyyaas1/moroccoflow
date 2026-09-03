package com.moroccoflow.traffic.generator.dto;

import com.moroccoflow.traffic.generator.TrafficScenario;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;

public record SimulatorStartRequest(
        TrafficScenario scenario,

        @Min(value = 1, message = "Interval must be at least 1 second")
        @Max(value = 60, message = "Interval must be at most 60 seconds")
        Integer intervalSeconds,

        Long cityId,

        List<Long> roadIds
) {
}
