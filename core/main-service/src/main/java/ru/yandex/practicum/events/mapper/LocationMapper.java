package ru.yandex.practicum.events.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.location.LocationDto;
import ru.yandex.practicum.events.model.Location;

@Component
public class LocationMapper {

    public static Location mapDtoToLocation(LocationDto dto) {
        return Location.builder()
                .lat(dto.getLat())
                .lon(dto.getLon())
                .build();
    }

    public static LocationDto mapLocationToDto(Location location) {
        return LocationDto.builder()
                .lat(location.getLat())
                .lon(location.getLon())
                .build();
    }
}
