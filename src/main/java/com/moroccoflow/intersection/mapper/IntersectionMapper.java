package com.moroccoflow.intersection.mapper;

import com.moroccoflow.city.entity.CityEntity;
import com.moroccoflow.intersection.dto.IntersectionRequest;
import com.moroccoflow.intersection.dto.IntersectionResponse;
import com.moroccoflow.intersection.entity.IntersectionEntity;

public class IntersectionMapper {

    public static IntersectionEntity toEntity(IntersectionRequest request, CityEntity city) {
        IntersectionEntity entity = new IntersectionEntity();
        entity.setCity(city);
        entity.setName(request.name());
        entity.setLatitude(request.latitude());
        entity.setLongitude(request.longitude());
        return entity;
    }

    public static IntersectionResponse toResponse(IntersectionEntity entity) {
        return new IntersectionResponse(
                entity.getId(),
                entity.getCity().getId(),
                entity.getName(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
