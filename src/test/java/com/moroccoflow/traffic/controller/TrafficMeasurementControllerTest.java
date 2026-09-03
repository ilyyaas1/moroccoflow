package com.moroccoflow.traffic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.moroccoflow.common.exception.GlobalExceptionHandler;
import com.moroccoflow.common.exception.NotFoundException;
import com.moroccoflow.traffic.dto.TrafficMeasurementRequest;
import com.moroccoflow.traffic.dto.TrafficMeasurementResponse;
import com.moroccoflow.traffic.service.TrafficMeasurementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrafficMeasurementController.class)
@Import(GlobalExceptionHandler.class)
class TrafficMeasurementControllerTest {

    private static final Instant T1 = Instant.parse("2026-08-23T08:30:00Z");

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @MockitoBean
    private TrafficMeasurementService trafficMeasurementService;

    @Test
    void create_validRequest_returns201() throws Exception {
        when(trafficMeasurementService.create(any(TrafficMeasurementRequest.class)))
                .thenReturn(sampleResponse());

        mockMvc.perform(post("/api/v1/traffic/measurements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/traffic/measurements/40"))
                .andExpect(jsonPath("$.roadId").value(12))
                .andExpect(jsonPath("$.vehicleCount").value(532));
    }

    @Test
    void create_unknownRoad_returns404() throws Exception {
        when(trafficMeasurementService.create(any(TrafficMeasurementRequest.class)))
                .thenThrow(new NotFoundException("Road with id 12 not found"));

        mockMvc.perform(post("/api/v1/traffic/measurements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Road with id 12 not found"));
    }

    @Test
    void create_negativeVehicleCount_returns400() throws Exception {
        TrafficMeasurementRequest request = new TrafficMeasurementRequest(
                12L, T1, -1, new BigDecimal("28.4"), new BigDecimal("67.2"), new BigDecimal("8.5"));

        mockMvc.perform(post("/api/v1/traffic/measurements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.vehicleCount").exists());
    }

    @Test
    void create_occupancyOver100_returns400() throws Exception {
        TrafficMeasurementRequest request = new TrafficMeasurementRequest(
                12L, T1, 532, new BigDecimal("28.4"), new BigDecimal("140"), new BigDecimal("8.5"));

        mockMvc.perform(post("/api/v1/traffic/measurements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.occupancyRate").exists());
    }

    @Test
    void findAll_returns200() throws Exception {
        when(trafficMeasurementService.findAll(eq(12L), eq(1L), eq(T1), isNull(), isNull()))
                .thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/v1/traffic/measurements")
                        .param("roadId", "12")
                        .param("cityId", "1")
                        .param("from", "2026-08-23T08:30:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(40));
    }

    @Test
    void findByRoad_returns200() throws Exception {
        when(trafficMeasurementService.findByRoadId(eq(12L), isNull(), isNull(), isNull()))
                .thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/v1/traffic/roads/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roadId").value(12));
    }

    @Test
    void findHistory_returns200() throws Exception {
        when(trafficMeasurementService.findByRoadId(eq(12L), isNull(), isNull(), eq(20)))
                .thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/v1/traffic/roads/12/history").param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(40));
    }

    @Test
    void findLatest_returns200() throws Exception {
        when(trafficMeasurementService.findLatestByRoadId(12L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/v1/traffic/roads/12/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleCount").value(532));
    }

    @Test
    void findLatest_empty_returns404() throws Exception {
        when(trafficMeasurementService.findLatestByRoadId(12L))
                .thenThrow(new NotFoundException("No traffic measurement found for road 12"));

        mockMvc.perform(get("/api/v1/traffic/roads/12/latest"))
                .andExpect(status().isNotFound());
    }

    private static TrafficMeasurementRequest sampleRequest() {
        return new TrafficMeasurementRequest(
                12L, T1, 532,
                new BigDecimal("28.4"),
                new BigDecimal("67.2"),
                new BigDecimal("8.5"));
    }

    private static TrafficMeasurementResponse sampleResponse() {
        return new TrafficMeasurementResponse(
                40L, 12L, 1L, T1, 532,
                new BigDecimal("28.4"),
                new BigDecimal("67.2"),
                new BigDecimal("8.5"),
                T1);
    }
}
