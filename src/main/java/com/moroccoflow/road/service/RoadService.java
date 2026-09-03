package com.moroccoflow.road.service;



import com.moroccoflow.city.entity.CityEntity;
import com.moroccoflow.city.repository.CityRepository;
import com.moroccoflow.common.exception.ConflictException;
import com.moroccoflow.common.exception.NotFoundException;
import com.moroccoflow.road.dto.RoadRequest;
import com.moroccoflow.road.dto.RoadResponse;
import com.moroccoflow.road.entity.RoadEntity;
import com.moroccoflow.road.mapper.RoadMapper;
import com.moroccoflow.road.repository.RoadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoadService {

    private final RoadRepository roadRepository;
    private final CityRepository cityRepository;

    public List<RoadResponse> findAll() {
        return roadRepository.findAll().stream()
                .map(RoadMapper::toResponse)
                .toList();
    }

    public List<RoadResponse> findByCityId(Long cityId) {
        return roadRepository.findByCityId(cityId).stream()
                .map(RoadMapper::toResponse)
                .toList();
    }

    public RoadResponse findById(Long id) {
        return RoadMapper.toResponse(findEntityOrThrow(id));
    }

    @Transactional
    public RoadResponse create(RoadRequest request) {
        CityEntity city = findCityOrThrow(request.cityId());
        assertUniqueName(request.cityId(), request.name(), null);
        RoadEntity entity = RoadMapper.toEntity(request, city);
        return RoadMapper.toResponse(roadRepository.save(entity));
    }

    @Transactional
    public RoadResponse update(Long id, RoadRequest request) {
        RoadEntity entity = findEntityOrThrow(id);
        CityEntity city = findCityOrThrow(request.cityId());
        assertUniqueName(request.cityId(), request.name(), id);
        entity.setCity(city);
        entity.setName(request.name());
        entity.setRoadType(request.roadType());
        entity.setSpeedLimitKm(request.speedLimitKm());
        if (request.lanes() != null) {
            entity.setLanes(request.lanes());
        }
        entity.setGeometry(request.geometry());
        return RoadMapper.toResponse(roadRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        if (!roadRepository.existsById(id)) {
            throw new NotFoundException("Road with id %d not found".formatted(id));
        }
        roadRepository.deleteById(id);
    }

    private RoadEntity findEntityOrThrow(Long id) {
        return roadRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Road with id %d not found".formatted(id)));
    }

    private CityEntity findCityOrThrow(Long cityId) {
        return cityRepository.findById(cityId)
                .orElseThrow(() -> new NotFoundException("City with id %d not found".formatted(cityId)));
    }

    private void assertUniqueName(Long cityId, String name, Long excludeId) {
        boolean duplicate = excludeId == null
                ? roadRepository.existsByCityIdAndName(cityId, name)
                : roadRepository.existsByCityIdAndNameAndIdNot(cityId, name, excludeId);
        if (duplicate) {
            throw new ConflictException(
                    "Road named '%s' already exists in city %d".formatted(name, cityId));
        }
    }
}