package com.moroccoflow.road.service;

import com.moroccoflow.city.entity.CityEntity;
import com.moroccoflow.city.repository.CityRepository;
import com.moroccoflow.common.exception.ConflictException;
import com.moroccoflow.common.exception.NotFoundException;
import com.moroccoflow.road.dto.RoadRequest;
import com.moroccoflow.road.dto.RoadResponse;
import com.moroccoflow.road.entity.RoadEntity;
import com.moroccoflow.road.entity.RoadEntity.RoadType;
import com.moroccoflow.road.repository.RoadRepository;
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
class RoadServiceTest {

    @Mock
    private RoadRepository roadRepository;

    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private RoadService roadService;

    @Test
    void findAll_returnsResponsesWithCityId() {
        when(roadRepository.findAll()).thenReturn(List.of(sampleRoad()));

        List<RoadResponse> result = roadService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("Avenue Mohammed V");
        assertThat(result.getFirst().cityId()).isEqualTo(1L);
    }

    @Test
    void findByCityId_filtersByCity() {
        when(roadRepository.findByCityId(1L)).thenReturn(List.of(sampleRoad()));

        List<RoadResponse> result = roadService.findByCityId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().cityId()).isEqualTo(1L);
    }

    @Test
    void findById_nonExistent_throwsNotFound() {
        when(roadRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roadService.findById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Road with id 999 not found");
    }

    @Test
    void create_unknownCity_throwsNotFound() {
        RoadRequest request = sampleRequest();
        when(cityRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roadService.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("City with id 1 not found");
        verify(roadRepository, never()).save(any());
    }

    @Test
    void create_duplicateName_throwsConflict() {
        when(cityRepository.findById(1L)).thenReturn(Optional.of(sampleCity()));
        when(roadRepository.existsByCityIdAndName(1L, "Avenue Mohammed V")).thenReturn(true);

        assertThatThrownBy(() -> roadService.create(sampleRequest()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Road named 'Avenue Mohammed V' already exists in city 1");
        verify(roadRepository, never()).save(any());
    }

    @Test
    void create_savesAndReturnsResponse() {
        when(cityRepository.findById(1L)).thenReturn(Optional.of(sampleCity()));
        when(roadRepository.existsByCityIdAndName(1L, "Avenue Mohammed V")).thenReturn(false);
        when(roadRepository.save(any(RoadEntity.class))).thenAnswer(inv -> {
            RoadEntity entity = inv.getArgument(0);
            entity.setId(10L);
            return entity;
        });

        RoadResponse result = roadService.create(sampleRequest());

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.name()).isEqualTo("Avenue Mohammed V");
        assertThat(result.roadType()).isEqualTo(RoadType.ARTERIAL);
        assertThat(result.lanes()).isEqualTo(2);
    }

    @Test
    void update_sameNameOnSameRoad_doesNotConflict() {
        RoadEntity existing = sampleRoad();
        when(roadRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(cityRepository.findById(1L)).thenReturn(Optional.of(sampleCity()));
        when(roadRepository.existsByCityIdAndNameAndIdNot(1L, "Avenue Mohammed V", 5L)).thenReturn(false);
        when(roadRepository.save(any(RoadEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        RoadResponse result = roadService.update(5L, sampleRequest());

        assertThat(result.name()).isEqualTo("Avenue Mohammed V");
        verify(roadRepository).existsByCityIdAndNameAndIdNot(1L, "Avenue Mohammed V", 5L);
    }

    @Test
    void delete_nonExistent_throwsNotFound() {
        when(roadRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> roadService.delete(999L))
                .isInstanceOf(NotFoundException.class);
        verify(roadRepository, never()).deleteById(anyLong());
    }

    private static RoadRequest sampleRequest() {
        return new RoadRequest(1L, "Avenue Mohammed V", RoadType.ARTERIAL, 50, 2, null);
    }

    private static CityEntity sampleCity() {
        return new CityEntity(1L, "Casablanca", "Morocco",
                new BigDecimal("33.573110"), new BigDecimal("-7.589843"), null, null);
    }

    private static RoadEntity sampleRoad() {
        return new RoadEntity(5L, sampleCity(), "Avenue Mohammed V", RoadType.ARTERIAL,
                50, 2, null, null, null);
    }
}
