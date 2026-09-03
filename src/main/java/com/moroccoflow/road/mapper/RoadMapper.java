package com.moroccoflow.road.mapper;



import com.moroccoflow.city.entity.CityEntity;
import com.moroccoflow.road.dto.RoadRequest;
import com.moroccoflow.road.dto.RoadResponse;
import com.moroccoflow.road.entity.RoadEntity;

public class RoadMapper {

    public static RoadEntity toEntity(RoadRequest request, CityEntity city) {
        RoadEntity entity = new RoadEntity();
        entity.setCity(city);
        entity.setName(request.name());
        entity.setRoadType(request.roadType());
        entity.setSpeedLimitKm(request.speedLimitKm());
        entity.setLanes(request.lanes() != null ? request.lanes() : 1);
        entity.setGeometry(request.geometry());
        return entity;
    }

    public static RoadResponse toResponse(RoadEntity entity) {
        return new RoadResponse(
                entity.getId(),
                entity.getCity().getId(),
                entity.getName(),
                entity.getRoadType(),
                entity.getSpeedLimitKm(),
                entity.getLanes(),
                entity.getGeometry(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}