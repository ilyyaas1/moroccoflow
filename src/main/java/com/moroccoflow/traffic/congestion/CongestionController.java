package com.moroccoflow.traffic.congestion;

import com.moroccoflow.traffic.congestion.dto.CongestionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/traffic")
@RequiredArgsConstructor
public class CongestionController {

    private final CongestionService congestionService;

    @GetMapping("/congestion")
    public List<CongestionResponse> findCurrent(@RequestParam(required = false) Long cityId) {
        return congestionService.findCurrent(cityId);
    }

    @GetMapping("/roads/{roadId}/congestion")
    public CongestionResponse forRoad(@PathVariable Long roadId) {
        return congestionService.forRoad(roadId);
    }
}
