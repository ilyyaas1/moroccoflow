package com.moroccoflow.road.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moroccoflow.common.exception.ConflictException;
import com.moroccoflow.common.exception.GlobalExceptionHandler;
import com.moroccoflow.common.exception.NotFoundException;
import com.moroccoflow.road.dto.RoadRequest;
import com.moroccoflow.road.dto.RoadResponse;
import com.moroccoflow.road.entity.RoadEntity.RoadType;
import com.moroccoflow.road.service.RoadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoadController.class)
@Import(GlobalExceptionHandler.class)
class RoadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private RoadService roadService;

    @Test
    void findAll_returns200() throws Exception {
        when(roadService.findAll()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/v1/roads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Avenue Mohammed V"));
    }

    @Test
    void findAll_withCityId_filters() throws Exception {
        when(roadService.findByCityId(1L)).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/v1/roads").param("cityId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cityId").value(1));
    }

    @Test
    void findById_nonExistent_returns404() throws Exception {
        when(roadService.findById(999L)).thenThrow(new NotFoundException("Road with id 999 not found"));

        mockMvc.perform(get("/api/v1/roads/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Road with id 999 not found"));
    }

    @Test
    void create_validRequest_returns201() throws Exception {
        RoadRequest request = new RoadRequest(1L, "Avenue Mohammed V", RoadType.ARTERIAL, 50, 2, null);
        when(roadService.create(any(RoadRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/v1/roads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/roads/5"));
    }

    @Test
    void create_duplicateName_returns409() throws Exception {
        RoadRequest request = new RoadRequest(1L, "Avenue Mohammed V", RoadType.ARTERIAL, 50, 2, null);
        when(roadService.create(any(RoadRequest.class)))
                .thenThrow(new ConflictException("Road named 'Avenue Mohammed V' already exists in city 1"));

        mockMvc.perform(post("/api/v1/roads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void create_missingName_returns400() throws Exception {
        RoadRequest request = new RoadRequest(1L, "", RoadType.ARTERIAL, 50, 2, null);

        mockMvc.perform(post("/api/v1/roads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @Test
    void update_returns200() throws Exception {
        RoadRequest request = new RoadRequest(1L, "Avenue Mohammed V", RoadType.ARTERIAL, 60, 3, null);
        when(roadService.update(eq(5L), any(RoadRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(put("/api/v1/roads/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void delete_returns204() throws Exception {
        doNothing().when(roadService).delete(5L);

        mockMvc.perform(delete("/api/v1/roads/5"))
                .andExpect(status().isNoContent());
    }

    private static RoadResponse sampleResponse() {
        return new RoadResponse(5L, 1L, "Avenue Mohammed V", RoadType.ARTERIAL,
                50, 2, null, Instant.now(), Instant.now());
    }
}
