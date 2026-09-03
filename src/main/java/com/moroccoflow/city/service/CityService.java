package com.moroccoflow.city.service;


import com.moroccoflow.city.dto.CityRequest;
import com.moroccoflow.city.dto.CityResponse;
import com.moroccoflow.city.entity.CityEntity;
import com.moroccoflow.city.mapper.CityMapper;
import com.moroccoflow.city.repository.CityRepository;
import com.moroccoflow.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;

    public List<CityResponse> findAll() {
        return cityRepository.findAll().stream()
                .map(CityMapper::toResponse)
                .toList();
    }

    public CityResponse findById(Long id) {
        return CityMapper.toResponse(findEntityOrThrow(id));
    }

    @Transactional
    public CityResponse create(CityRequest request) {
        CityEntity entity = CityMapper.toEntity(request);
        return CityMapper.toResponse(cityRepository.save(entity));
    }

    @Transactional
    public CityResponse update(Long id, CityRequest request) {
        CityEntity entity = findEntityOrThrow(id);
        entity.setName(request.name());
        if (request.country() != null) {
            entity.setCountry(request.country());
        }
        entity.setLatitude(request.latitude());
        entity.setLongitude(request.longitude());
        return CityMapper.toResponse(cityRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        if (!cityRepository.existsById(id)) {
            throw new NotFoundException("City with id %d not found".formatted(id));
        }
        cityRepository.deleteById(id);
    }

    private CityEntity findEntityOrThrow(Long id) {
        return cityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("City with id %d not found".formatted(id)));
    }
}