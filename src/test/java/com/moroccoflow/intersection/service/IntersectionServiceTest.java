package com.moroccoflow.intersection.service;

import com.moroccoflow.city.entity.CityEntity;
import com.moroccoflow.city.repository.CityRepository;
import com.moroccoflow.common.exception.ConflictException;
import com.moroccoflow.common.exception.NotFoundException;
import com.moroccoflow.intersection.dto.IntersectionRequest;
import com.moroccoflow.intersection.dto.IntersectionResponse;
import com.moroccoflow.intersection.entity.IntersectionEntity;
import com.moroccoflow.intersection.repository.IntersectionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntersectionServiceTest {

    @Mock
    private IntersectionRepository intersectionRepository;

    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private IntersectionService intersectionService;

    @Test
    void findAll_returnsResponsesWithCityId() {
        when(intersectionRepository.findAll()).thenReturn(List.of(sampleIntersection()));

        List<IntersectionResponse> result = intersectionService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("Place des Nations Unies");
        assertThat(result.getFirst().cityId()).isEqualTo(1L);
    }

    @Test
    void findByCityId_filtersByCity() {
        when(intersectionRepository.findByCityId(1L)).thenReturn(List.of(sampleIntersection()));

        assertThat(intersectionService.findByCityId(1L)).hasSize(1);
    }

    @Test
    void findById_nonExistent_throwsNotFound() {
        when(intersectionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> intersectionService.findById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Intersection with id 999 not found");
    }

    @Test
    void create_unknownCity_throwsNotFound() {
        when(cityRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> intersectionService.create(sampleRequest()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("City with id 1 not found");
        verify(intersectionRepository, never()).save(any());
    }

    @Test
    void create_duplicateName_throwsConflict() {
        when(cityRepository.findById(1L)).thenReturn(Optional.of(sampleCity()));
        when(intersectionRepository.existsByCityIdAndName(1L, "Place des Nations Unies")).thenReturn(true);

        assertThatThrownBy(() -> intersectionService.create(sampleRequest()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Intersection named 'Place des Nations Unies' already exists in city 1");
        verify(intersectionRepository, never()).save(any());
    }

    @Test
    void create_savesAndReturnsResponse() {
        when(cityRepository.findById(1L)).thenReturn(Optional.of(sampleCity()));
        when(intersectionRepository.existsByCityIdAndName(1L, "Place des Nations Unies")).thenReturn(false);
        when(intersectionRepository.save(any(IntersectionEntity.class))).thenAnswer(inv -> {
            IntersectionEntity entity = inv.getArgument(0);
            entity.setId(8L);
            return entity;
        });

        IntersectionResponse result = intersectionService.create(sampleRequest());

        assertThat(result.id()).isEqualTo(8L);
        assertThat(result.cityId()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Place des Nations Unies");
    }

    @Test
    void delete_existing_callsDeleteById() {
        when(intersectionRepository.existsById(8L)).thenReturn(true);

        intersectionService.delete(8L);

        verify(intersectionRepository).deleteById(8L);
    }

    private static IntersectionRequest sampleRequest() {
        return new IntersectionRequest(1L, "Place des Nations Unies",
                new BigDecimal("33.595000"), new BigDecimal("-7.618000"));
    }

    private static CityEntity sampleCity() {
        return new CityEntity(1L, "Casablanca", "Morocco",
                new BigDecimal("33.573110"), new BigDecimal("-7.589843"), null, null);
    }

    private static IntersectionEntity sampleIntersection() {
        return new IntersectionEntity(8L, sampleCity(), "Place des Nations Unies",
                new BigDecimal("33.595000"), new BigDecimal("-7.618000"), null, null);
    }
}
