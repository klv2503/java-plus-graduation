package ru.yandex.practicum.events.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.events.client.EventCategoryFeign;
import ru.yandex.practicum.events.client.EventRequestFeign;
import ru.yandex.practicum.clients.UserServiceFeign;
import ru.yandex.practicum.config.DateConfig;
import ru.yandex.practicum.dto.request.ChangeRequestStatus;
import ru.yandex.practicum.errors.exceptions.ForbiddenActionException;
import ru.yandex.practicum.dto.events.EventFullDto;
import ru.yandex.practicum.dto.events.EventShortDto;
import ru.yandex.practicum.dto.events.NewEventDto;
import ru.yandex.practicum.dto.events.UpdateEventUserRequest;
import ru.yandex.practicum.events.mapper.LocationMapper;
import ru.yandex.practicum.events.model.Event;
import ru.yandex.practicum.enums.StateEvent;
import ru.yandex.practicum.events.mapper.EventMapper;
import ru.yandex.practicum.events.repository.EventRepository;
import ru.yandex.practicum.events.dto.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.yandex.practicum.dto.events.GetUserEventsDto;
import ru.yandex.practicum.dto.request.ParticipationRequestDto;
import ru.yandex.practicum.enums.ParticipationRequestStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PrivateUserEventServiceImpl implements PrivateUserEventService {
    private final EventRepository eventRepository;
    private final UserServiceFeign userServiceFeign; //используется для проверки существования юзера
    private final EventCategoryFeign categoryFeign;
    private final EventRequestFeign requestFeign;

    @Override
    public List<EventShortDto> getUsersEvents(GetUserEventsDto dto) {
        userServiceFeign.getUserById(dto.getUserId());
        PageRequest page = PageRequest.of(dto.getFrom() > 0 ? dto.getFrom() / dto.getSize() : 0, dto.getSize());
        return eventRepository.findAllByInitiatorId(dto.getUserId(), page).stream()
                .map(EventMapper::toEventShortDto)
                .toList();
    }

    @Override
    public EventFullDto getUserEventById(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));
        return EventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto addNewEvent(Long userId, NewEventDto eventDto) {
        userServiceFeign.getUserById(userId);
        Event event = EventMapper.dtoToEvent(eventDto, userId);

        eventRepository.save(event);

        return EventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest updateDto) {
        userServiceFeign.getUserById(userId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));

        if (!Objects.equals(event.getInitiatorId(), userId)) {
            throw new ForbiddenActionException("User is not the event creator");
        }

        if (Objects.equals(event.getState(), StateEvent.PUBLISHED)) {
            throw new ForbiddenActionException("Changing of published event is forbidden.");
        }
        Optional.ofNullable(updateDto.getTitle()).ifPresent(event::setTitle);
        Optional.ofNullable(updateDto.getAnnotation()).ifPresent(event::setAnnotation);
        Optional.ofNullable(updateDto.getDescription()).ifPresent(event::setDescription);
        Optional.ofNullable(updateDto.getEventDate()).map(this::parseEventDate).ifPresent(event::setEventDate);
        Optional.ofNullable(updateDto.getLocation()).map(LocationMapper::mapDtoToLocation).ifPresent(event::setLocation);

        if (updateDto.getCategory() != null) {
            categoryFeign.getInfoById(updateDto.getCategory()); //проверка существования
            event.setCategory(updateDto.getCategory());
        }

        updateEventState(event, updateDto.getStateAction());

        event.setRequestModeration(updateDto.isRequestModeration());
        event.setInitiatorId(userId);

        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<ParticipationRequestDto> getUserEventRequests(Long userId, Long eventId) {
        eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId + " for initiator " + userId));
        return requestFeign.getRequestList(eventId).getBody();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateUserEventRequest(Long userId, Long eventId, EventRequestStatusUpdateRequest request) {
        //Проверки существования
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with " + eventId + " not found"));
        userServiceFeign.getUserById(userId);
        ChangeRequestStatus changeRequestStatus = ChangeRequestStatus.builder()
                .event(eventId)
                .limit(event.getParticipantLimit())
                .requestIds(request.getRequestIds())
                .status(ParticipationRequestStatus.valueOf(request.getStatus()))
                .build();
        return requestFeign.changeRequestStatuses(changeRequestStatus).getBody();
    }

    private LocalDateTime parseEventDate(String date) {
        return LocalDateTime.parse(date, DateConfig.FORMATTER);
    }

    private void updateEventState(Event event, String stateAction) {
        if (stateAction == null) return;

        if ("PUBLISH_REVIEW".equals(stateAction)) {
            throw new ForbiddenActionException("Publishing this event is forbidden.");
        }

        switch (stateAction) {
            case "CANCEL_REVIEW":
                event.setState(StateEvent.CANCELED);
                break;
            case "SEND_TO_REVIEW":
                event.setState(StateEvent.PENDING);
                break;
            default:
                throw new IllegalArgumentException("Invalid state action: " + stateAction);
        }
    }

}