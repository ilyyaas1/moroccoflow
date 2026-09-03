package com.moroccoflow.traffic.congestion;

import com.moroccoflow.common.exception.GlobalExceptionHandler;
import com.moroccoflow.common.exception.NotFoundException;
import com.moroccoflow.traffic.congestion.dto.CongestionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CongestionController.class)
@Import(GlobalExceptionHandler.class)
class CongestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CongestionService congestionService;

    @Test
    void forRoad_returns200() throws Exception {
        when(congestionService.forRoad(12L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/v1/traffic/roads/12/congestion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roadId").value(12))
                .andExpect(jsonPath("$.level").value("MODERATE"))
                .andExpect(jsonPath("$.congestionIndex").value(0.528));
    }

    @Test
    void forRoad_noMeasurement_returns404() throws Exception {
        when(congestionService.forRoad(12L))
                .thenThrow(new NotFoundException("No traffic measurement found for road 12"));

        mockMvc.perform(get("/api/v1/traffic/roads/12/congestion"))
                .andExpect(status().isNotFound());
    }

    @Test
    void findCurrent_returns200() throws Exception {
        when(congestionService.findCurrent(1L)).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/v1/traffic/congestion").param("cityId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cityId").value(1));
    }

    private static CongestionResponse sampleResponse() {
        return new CongestionResponse(
                12L, 1L, Instant.parse("2026-08-23T08:30:00Z"),
                new BigDecimal("0.5280"), CongestionLevel.MODERATE,
                new BigDecimal("28.4"), new BigDecimal("67.2"), 50);
    }
}
