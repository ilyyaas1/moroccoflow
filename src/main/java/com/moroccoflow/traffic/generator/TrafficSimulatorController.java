package com.moroccoflow.traffic.generator;

import com.moroccoflow.traffic.generator.dto.SimulatorScenarioRequest;
import com.moroccoflow.traffic.generator.dto.SimulatorStartRequest;
import com.moroccoflow.traffic.generator.dto.SimulatorStatusResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/simulator")
@RequiredArgsConstructor
public class TrafficSimulatorController {

    private final TrafficSimulator trafficSimulator;

    @GetMapping("/status")
    public SimulatorStatusResponse status() {
        return trafficSimulator.status();
    }

    @PostMapping("/start")
    public SimulatorStatusResponse start(@Valid @RequestBody(required = false) SimulatorStartRequest request) {
        SimulatorStartRequest body = request != null
                ? request
                : new SimulatorStartRequest(null, null, null, null);
        return trafficSimulator.start(body);
    }

    @PostMapping("/stop")
    public SimulatorStatusResponse stop() {
        return trafficSimulator.stop();
    }

    @PostMapping("/scenario")
    public SimulatorStatusResponse changeScenario(@Valid @RequestBody SimulatorScenarioRequest request) {
        return trafficSimulator.changeScenario(request.scenario());
    }
}
