package com.moroccoflow.traffic.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moroccoflow.common.exception.ConflictException;
import com.moroccoflow.common.exception.GlobalExceptionHandler;
import com.moroccoflow.traffic.generator.dto.SimulatorScenarioRequest;
import com.moroccoflow.traffic.generator.dto.SimulatorStartRequest;
import com.moroccoflow.traffic.generator.dto.SimulatorStatusResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrafficSimulatorController.class)
@Import(GlobalExceptionHandler.class)
class TrafficSimulatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private TrafficSimulator trafficSimulator;

    @Test
    void status_returns200() throws Exception {
        when(trafficSimulator.status()).thenReturn(idleStatus());

        mockMvc.perform(get("/api/v1/simulator/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.running").value(false));
    }

    @Test
    void start_returns200() throws Exception {
        when(trafficSimulator.start(any(SimulatorStartRequest.class))).thenReturn(
                new SimulatorStatusResponse(true, TrafficScenario.RUSH_HOUR, 5, List.of(12L)));

        mockMvc.perform(post("/api/v1/simulator/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"scenario":"RUSH_HOUR","intervalSeconds":5,"roadIds":[12]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.running").value(true))
                .andExpect(jsonPath("$.scenario").value("RUSH_HOUR"));
    }

    @Test
    void start_noRoads_returns409() throws Exception {
        when(trafficSimulator.start(any(SimulatorStartRequest.class)))
                .thenThrow(new ConflictException("No roads available to simulate. Create at least one road first."));

        mockMvc.perform(post("/api/v1/simulator/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict());
    }

    @Test
    void start_intervalTooHigh_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/simulator/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"intervalSeconds\":120}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.intervalSeconds").exists());
    }

    @Test
    void stop_returns200() throws Exception {
        when(trafficSimulator.stop()).thenReturn(idleStatus());

        mockMvc.perform(post("/api/v1/simulator/stop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.running").value(false));
    }

    @Test
    void scenario_returns200() throws Exception {
        when(trafficSimulator.changeScenario(TrafficScenario.ACCIDENT)).thenReturn(
                new SimulatorStatusResponse(true, TrafficScenario.ACCIDENT, 5, List.of(12L)));

        mockMvc.perform(post("/api/v1/simulator/scenario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SimulatorScenarioRequest(TrafficScenario.ACCIDENT))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scenario").value("ACCIDENT"));
    }

    private static SimulatorStatusResponse idleStatus() {
        return new SimulatorStatusResponse(false, TrafficScenario.NORMAL, 5, List.of());
    }
}
