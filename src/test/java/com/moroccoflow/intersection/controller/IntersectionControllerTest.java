package com.moroccoflow.intersection.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moroccoflow.common.exception.ConflictException;
import com.moroccoflow.common.exception.GlobalExceptionHandler;
import com.moroccoflow.common.exception.NotFoundException;
import com.moroccoflow.intersection.dto.IntersectionRequest;
import com.moroccoflow.intersection.dto.IntersectionResponse;
import com.moroccoflow.intersection.service.IntersectionService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IntersectionController.class)
@Import(GlobalExceptionHandler.class)
class IntersectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private IntersectionService intersectionService;

    @Test
    void findAll_returns200() throws Exception {
        when(intersectionService.findAll()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/v1/intersections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Place des Nations Unies"));
    }

    @Test
    void findAll_withCityId_filters() throws Exception {
        when(intersectionService.findByCityId(1L)).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/v1/intersections").param("cityId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cityId").value(1));
    }

    @Test
    void findById_nonExistent_returns404() throws Exception {
        when(intersectionService.findById(999L))
                .thenThrow(new NotFoundException("Intersection with id 999 not found"));

        mockMvc.perform(get("/api/v1/intersections/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_validRequest_returns201() throws Exception {
        IntersectionRequest request = sampleRequest();
        when(intersectionService.create(any(IntersectionRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/v1/intersections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/intersections/8"));
    }

    @Test
    void create_duplicateName_returns409() throws Exception {
        when(intersectionService.create(any(IntersectionRequest.class)))
                .thenThrow(new ConflictException(
                        "Intersection named 'Place des Nations Unies' already exists in city 1"));

        mockMvc.perform(post("/api/v1/intersections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isConflict());
    }

    @Test
    void create_invalidLatitude_returns400() throws Exception {
        IntersectionRequest request = new IntersectionRequest(1L, "Place des Nations Unies",
                new BigDecimal("999"), new BigDecimal("-7.618000"));

        mockMvc.perform(post("/api/v1/intersections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.latitude").exists());
    }

    @Test
    void update_returns200() throws Exception {
        when(intersectionService.update(eq(8L), any(IntersectionRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(put("/api/v1/intersections/8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8));
    }

    @Test
    void delete_returns204() throws Exception {
        doNothing().when(intersectionService).delete(8L);

        mockMvc.perform(delete("/api/v1/intersections/8"))
                .andExpect(status().isNoContent());
    }

    private static IntersectionRequest sampleRequest() {
        return new IntersectionRequest(1L, "Place des Nations Unies",
                new BigDecimal("33.595000"), new BigDecimal("-7.618000"));
    }

    private static IntersectionResponse sampleResponse() {
        return new IntersectionResponse(8L, 1L, "Place des Nations Unies",
                new BigDecimal("33.595000"), new BigDecimal("-7.618000"),
                Instant.now(), Instant.now());
    }
}
