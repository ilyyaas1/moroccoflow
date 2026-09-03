package com.moroccoflow.traffic.generator.dto;

import com.moroccoflow.traffic.generator.TrafficScenario;
import jakarta.validation.constraints.NotNull;

public record SimulatorScenarioRequest(
        @NotNull(message = "Scenario is required")
        TrafficScenario scenario
) {
}
