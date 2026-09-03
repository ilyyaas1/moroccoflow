package com.moroccoflow.city.mapper;



import com.moroccoflow.city.dto.CityRequest;
import com.moroccoflow.city.dto.CityResponse;
import com.moroccoflow.city.entity.CityEntity;

public class CityMapper {

    public static CityEntity toEntity(CityRequest request) {
        CityEntity entity = new CityEntity();
        entity.setName(request.name());
        entity.setCountry(request.country() != null ? request.country() : "Morocco");
        entity.setLatitude(request.latitude());
        entity.setLongitude(request.longitude());
        return entity;
    }

    public static CityResponse toResponse(CityEntity entity) {
        return new CityResponse(
                entity.getId(),
                entity.getName(),
                entity.getCountry(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}