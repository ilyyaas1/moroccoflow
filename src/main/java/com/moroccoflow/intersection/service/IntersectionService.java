package com.moroccoflow.intersection.service;

import com.moroccoflow.city.entity.CityEntity;
import com.moroccoflow.city.repository.CityRepository;
import com.moroccoflow.common.exception.ConflictException;
import com.moroccoflow.common.exception.NotFoundException;
import com.moroccoflow.intersection.dto.IntersectionRequest;
import com.moroccoflow.intersection.dto.IntersectionResponse;
import com.moroccoflow.intersection.entity.IntersectionEntity;
import com.moroccoflow.intersection.mapper.IntersectionMapper;
import com.moroccoflow.intersection.repository.IntersectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IntersectionService {

    private final IntersectionRepository intersectionRepository;
    private final CityRepository cityRepository;

    public List<IntersectionResponse> findAll() {
        return intersectionRepository.findAll().stream()
                .map(IntersectionMapper::toResponse)
                .toList();
    }

    public List<IntersectionResponse> findByCityId(Long cityId) {
        return intersectionRepository.findByCityId(cityId).stream()
                .map(IntersectionMapper::toResponse)
                .toList();
    }

    public IntersectionResponse findById(Long id) {
        return IntersectionMapper.toResponse(findEntityOrThrow(id));
    }

    @Transactional
    public IntersectionResponse create(IntersectionRequest request) {
        CityEntity city = findCityOrThrow(request.cityId());
        assertUniqueName(request.cityId(), request.name(), null);
        IntersectionEntity entity = IntersectionMapper.toEntity(request, city);
        return IntersectionMapper.toResponse(intersectionRepository.save(entity));
    }

    @Transactional
    public IntersectionResponse update(Long id, IntersectionRequest request) {
        IntersectionEntity entity = findEntityOrThrow(id);
        CityEntity city = findCityOrThrow(request.cityId());
        assertUniqueName(request.cityId(), request.name(), id);
        entity.setCity(city);
        entity.setName(request.name());
        entity.setLatitude(request.latitude());
        entity.setLongitude(request.longitude());
        return IntersectionMapper.toResponse(intersectionRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        if (!intersectionRepository.existsById(id)) {
            throw new NotFoundException("Intersection with id %d not found".formatted(id));
        }
        intersectionRepository.deleteById(id);
    }

    private IntersectionEntity findEntityOrThrow(Long id) {
        return intersectionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Intersection with id %d not found".formatted(id)));
    }

    private CityEntity findCityOrThrow(Long cityId) {
        return cityRepository.findById(cityId)
                .orElseThrow(() -> new NotFoundException("City with id %d not found".formatted(cityId)));
    }

    private void assertUniqueName(Long cityId, String name, Long excludeId) {
        boolean duplicate = excludeId == null
                ? intersectionRepository.existsByCityIdAndName(cityId, name)
                : intersectionRepository.existsByCityIdAndNameAndIdNot(cityId, name, excludeId);
        if (duplicate) {
            throw new ConflictException(
                    "Intersection named '%s' already exists in city %d".formatted(name, cityId));
        }
    }
}