package ru.yandex.practicum.events.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.config.DateConfig;
import ru.yandex.practicum.dto.events.EventFullDto;
import ru.yandex.practicum.dto.events.EventShortDto;
import ru.yandex.practicum.dto.events.NewEventDto;
import ru.yandex.practicum.enums.StateEvent;
import ru.yandex.practicum.events.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventMapper {
    public static NewEventDto toNewEventDto(Event event) {
        return NewEventDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(event.getCategory())
                .description(event.getDescription())
                .eventDate(event.getEventDate().format(DateConfig.FORMATTER))
                .location(LocationMapper.mapLocationToDto(event.getLocation()))
                .paid(event.isPaid())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.isRequestModeration())
                .title(event.getTitle())
                .build();
    }

    public static Event dtoToEvent(NewEventDto dto, Long userId) {
        LocalDateTime eventTime = LocalDateTime.parse(dto.getEventDate(), DateConfig.FORMATTER);
        return Event.builder()
                .id(dto.getId())
                .annotation(dto.getAnnotation())
                .title(dto.getTitle())
                .category(dto.getCategory())
                .description(dto.getDescription())
                .eventDate(eventTime)
                .location(LocationMapper.mapDtoToLocation(dto.getLocation()))
                .paid(dto.isPaid())
                .participantLimit(Objects.nonNull(dto.getParticipantLimit()) ? dto.getParticipantLimit() : 0)
                .requestModeration(Objects.nonNull(dto.getRequestModeration()) ? dto.getRequestModeration() : true)
                .initiatorId(userId)
                .createdOn(LocalDateTime.now())
                .publishedOn(LocalDateTime.now())
                .state(StateEvent.PENDING)
                .rating(0.0)
                .build();
    }

    public static EventFullDto toEventFullDto(Event event) {
        String publishedOn = event.getPublishedOn() == null ?
                null :
                event.getPublishedOn().format(DateConfig.FORMATTER);

        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(event.getCategory())
                .confirmedRequests((event.getConfirmedRequests() == null) ? 0 : event.getConfirmedRequests())
                .eventDate(event.getEventDate().format(DateConfig.FORMATTER))
                .initiator(event.getInitiatorId())
                .paid(event.isPaid())
                .title(event.getTitle())
                .rating((event.getRating() == null) ? 0 : event.getRating())
                .createdOn(event.getCreatedOn().format(DateConfig.FORMATTER))
                .description(event.getDescription())
                .location(LocationMapper.mapLocationToDto(event.getLocation()))
                .participantLimit(event.getParticipantLimit())
                .publishedOn(publishedOn)
                .requestModeration(event.isRequestModeration())
                .state(event.getState())
                .build();
    }

    public static EventShortDto toEventShortDto(Event event) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(event.getCategory())
                .confirmedRequests((event.getConfirmedRequests() == null) ? 0 : event.getConfirmedRequests())
                .eventDate(event.getEventDate().format(DateConfig.FORMATTER))
                .initiator(event.getInitiatorId())
                .paid(event.isPaid())
                .title(event.getTitle())
                .rating((event.getRating() == null) ? 0 : event.getRating())
                .build();
    }

    public static List<EventShortDto> toListEventShortDto(List<Event> events) {
        return events.stream()
                .map(EventMapper::toEventShortDto)
                .toList();
    }

}