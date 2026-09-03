package com.moroccoflow.traffic.generator.dto;

import com.moroccoflow.traffic.generator.TrafficScenario;

import java.util.List;

public record SimulatorStatusResponse(
        boolean running,
        TrafficScenario scenario,
        int intervalSeconds,
        List<Long> roadIds
) {
}
