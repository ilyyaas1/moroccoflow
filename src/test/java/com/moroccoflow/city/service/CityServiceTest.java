package com.moroccoflow.city.service;



import com.moroccoflow.city.service.CityService;
import com.moroccoflow.city.dto.CityRequest;
import com.moroccoflow.city.dto.CityResponse;
import com.moroccoflow.city.entity.CityEntity;
import com.moroccoflow.city.repository.CityRepository;
import com.moroccoflow.common.exception.NotFoundException;
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
class CityServiceTest {

    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private CityService cityService;

    @Test
    void findAll_returnsListOfResponses() {
        CityEntity city = new CityEntity(1L, "Casablanca", "Morocco",
                new BigDecimal("33.573110"), new BigDecimal("-7.589843"), null, null);
        when(cityRepository.findAll()).thenReturn(List.of(city));

        List<CityResponse> result = cityService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Casablanca");
    }

    @Test
    void findById_existingId_returnsResponse() {
        CityEntity city = new CityEntity(1L, "Casablanca", "Morocco",
                new BigDecimal("33.573110"), new BigDecimal("-7.589843"), null, null);
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));

        CityResponse result = cityService.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Casablanca");
    }

    @Test
    void findById_nonExistentId_throwsNotFoundException() {
        when(cityRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cityService.findById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("City with id 999 not found");
    }

    @Test
    void create_savesAndReturnsResponse() {
        CityRequest request = new CityRequest("Rabat", "Morocco",
                new BigDecimal("34.020882"), new BigDecimal("-6.841650"));
        when(cityRepository.save(any(CityEntity.class)))
                .thenAnswer(inv -> {
                    CityEntity e = inv.getArgument(0);
                    e.setId(1L);
                    return e;
                });

        CityResponse result = cityService.create(request);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Rabat");
        assertThat(result.country()).isEqualTo("Morocco");
        verify(cityRepository).save(any(CityEntity.class));
    }

    @Test
    void create_countryDefaultsToMorocco_whenNull() {
        CityRequest request = new CityRequest("Rabat", null,
                new BigDecimal("34.020882"), new BigDecimal("-6.841650"));
        when(cityRepository.save(any(CityEntity.class)))
                .thenAnswer(inv -> {
                    CityEntity e = inv.getArgument(0);
                    e.setId(1L);
                    return e;
                });

        CityResponse result = cityService.create(request);

        assertThat(result.country()).isEqualTo("Morocco");
    }

    @Test
    void delete_existingId_callsDeleteById() {
        when(cityRepository.existsById(1L)).thenReturn(true);

        cityService.delete(1L);

        verify(cityRepository).deleteById(1L);
    }

    @Test
    void delete_nonExistentId_throwsNotFoundException() {
        when(cityRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> cityService.delete(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("City with id 999 not found");
        verify(cityRepository, never()).deleteById(anyLong());
    }

    @Test
    void update_existingId_updatesFields() {
        CityEntity city = new CityEntity(1L, "Casablanca", "Morocco",
                new BigDecimal("33.573110"), new BigDecimal("-7.589843"), null, null);
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(cityRepository.save(any(CityEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        CityRequest request = new CityRequest("Rabat", "Morocco",
                new BigDecimal("34.020882"), new BigDecimal("-6.841650"));

        CityResponse result = cityService.update(1L, request);

        assertThat(result.name()).isEqualTo("Rabat");
        assertThat(result.latitude()).isEqualByComparingTo("34.020882");
    }
}