package ru.practicum.users.service;

import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.dto.NewEventDto;
import ru.practicum.events.dto.UpdateEventUserRequest;
import ru.practicum.users.dto.EventRequestStatusUpdateRequest;
import ru.practicum.users.dto.EventRequestStatusUpdateResult;
import ru.practicum.users.dto.GetUserEventsDto;
import ru.practicum.users.dto.ParticipationRequestDto;

import java.util.List;

public interface PrivateUserEventService {
    List<EventShortDto> getUsersEvents(GetUserEventsDto dto);

    EventFullDto getUserEventById(Long userId, Long eventId);

    EventFullDto addNewEvent(Long userId, NewEventDto eventDto);

    EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest updateDto);

    List<ParticipationRequestDto> getUserEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateUserEventRequest(Long userId, Long eventId, EventRequestStatusUpdateRequest request);
}
