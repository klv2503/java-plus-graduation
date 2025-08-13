package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.events.UpdateEventAdminRequest;
import ru.yandex.practicum.dto.events.EventFullDto;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminEventService {

    List<EventFullDto> getEvents(
            List<Long> users,
            List<String> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            int from,
            int size
    );

    EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    EventFullDto getEvent(Long id);
}
