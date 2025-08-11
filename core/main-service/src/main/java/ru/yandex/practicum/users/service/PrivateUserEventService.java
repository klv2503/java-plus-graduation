package ru.yandex.practicum.users.service;

import ru.yandex.practicum.dto.events.EventFullDto;
import ru.yandex.practicum.dto.events.EventShortDto;
import ru.yandex.practicum.dto.events.NewEventDto;
import ru.yandex.practicum.dto.events.UpdateEventUserRequest;
import ru.yandex.practicum.users.dto.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.users.dto.EventRequestStatusUpdateResult;
import ru.yandex.practicum.users.dto.GetUserEventsDto;
import ru.yandex.practicum.users.dto.ParticipationRequestDto;

import java.util.List;

public interface PrivateUserEventService {
    List<EventShortDto> getUsersEvents(GetUserEventsDto dto);

    EventFullDto getUserEventById(Long userId, Long eventId);

    EventFullDto addNewEvent(Long userId, NewEventDto eventDto);

    EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest updateDto);

    List<ParticipationRequestDto> getUserEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateUserEventRequest(Long userId, Long eventId, EventRequestStatusUpdateRequest request);
}
