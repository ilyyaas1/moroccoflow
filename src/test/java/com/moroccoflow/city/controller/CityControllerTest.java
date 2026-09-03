package com.moroccoflow.city.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.moroccoflow.city.controller.CityController;
import com.moroccoflow.city.dto.CityRequest;
import com.moroccoflow.city.dto.CityResponse;
import com.moroccoflow.city.service.CityService;
import com.moroccoflow.common.exception.GlobalExceptionHandler;
import com.moroccoflow.common.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest(CityController.class)
@Import(GlobalExceptionHandler.class)
class CityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CityService cityService;

    @Test
    void findAll_returns200() throws Exception {
        when(cityService.findAll()).thenReturn(List.of(
                new CityResponse(1L, "Casablanca", "Morocco",
                        new BigDecimal("33.573110"), new BigDecimal("-7.589843"),
                        Instant.now(), Instant.now())
        ));

        mockMvc.perform(get("/api/v1/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Casablanca"));
    }

    @Test
    void findById_existing_returns200() throws Exception {
        when(cityService.findById(1L)).thenReturn(
                new CityResponse(1L, "Casablanca", "Morocco",
                        new BigDecimal("33.573110"), new BigDecimal("-7.589843"),
                        Instant.now(), Instant.now())
        );

        mockMvc.perform(get("/api/v1/cities/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Casablanca"));
    }

    @Test
    void findById_nonExistent_returns404() throws Exception {
        when(cityService.findById(999L))
                .thenThrow(new NotFoundException("City with id 999 not found"));

        mockMvc.perform(get("/api/v1/cities/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("City with id 999 not found"));
    }

    @Test
    void create_validRequest_returns201() throws Exception {
        CityRequest request = new CityRequest("Rabat", "Morocco",
                new BigDecimal("34.020882"), new BigDecimal("-6.841650"));
        when(cityService.create(any(CityRequest.class))).thenReturn(
                new CityResponse(1L, "Rabat", "Morocco",
                        new BigDecimal("34.020882"), new BigDecimal("-6.841650"),
                        Instant.now(), Instant.now())
        );

        mockMvc.perform(post("/api/v1/cities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Rabat"))
                .andExpect(header().string("Location", "/api/v1/cities/1"));
    }

    @Test
    void create_blankName_returns400() throws Exception {
        CityRequest request = new CityRequest("", "Morocco",
                new BigDecimal("34.020882"), new BigDecimal("-6.841650"));

        mockMvc.perform(post("/api/v1/cities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @Test
    void create_invalidLatitude_returns400() throws Exception {
        CityRequest request = new CityRequest("Rabat", "Morocco",
                new BigDecimal("999"), new BigDecimal("-6.841650"));

        mockMvc.perform(post("/api/v1/cities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.latitude").exists());
    }

    @Test
    void delete_existing_returns204() throws Exception {
        doNothing().when(cityService).delete(1L);

        mockMvc.perform(delete("/api/v1/cities/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void update_existing_returns200() throws Exception {
        CityRequest request = new CityRequest("Rabat", "Morocco",
                new BigDecimal("34.020882"), new BigDecimal("-6.841650"));
        when(cityService.update(eq(1L), any(CityRequest.class))).thenReturn(
                new CityResponse(1L, "Rabat", "Morocco",
                        new BigDecimal("34.020882"), new BigDecimal("-6.841650"),
                        Instant.now(), Instant.now())
        );

        mockMvc.perform(put("/api/v1/cities/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Rabat"));
    }
}
