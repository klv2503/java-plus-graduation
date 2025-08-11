package ru.yandex.practicum.users.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.client.UserServiceFeign;
import ru.yandex.practicum.dto.user.UserDto;
import ru.yandex.practicum.events.model.Event;
import ru.yandex.practicum.events.repository.EventRepository;
import ru.yandex.practicum.users.dto.ParticipationRequestDto;
import ru.yandex.practicum.users.mapper.ParticipationRequestToDtoMapper;
import ru.yandex.practicum.users.model.ParticipationRequest;
import ru.yandex.practicum.users.model.ParticipationRequestStatus;
import ru.yandex.practicum.users.repository.ParticipationRequestRepository;
import ru.yandex.practicum.users.validation.ParticipationRequestValidator;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestService {

    private final ParticipationRequestRepository requestRepository;
    private final EventRepository eventRepository;

    private final UserServiceFeign userServiceFeign;

    private final ParticipationRequestValidator participationRequestValidator;

    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        userServiceFeign.getUserById(userId);
        return requestRepository.findByUserId(userId)
                .stream()
                .map(ParticipationRequestToDtoMapper::mapToDto)
                .toList();
    }

    public int getConfirmedRequests(long eventId) {
        return requestRepository
                .countConfirmedRequestsByStatusAndEventId(ParticipationRequestStatus.CONFIRMED, eventId);
    }

    @Transactional
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        UserDto user = userServiceFeign.getUserById(userId).getBody();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id=" + eventId + " was not found"));
        long confirmedRequestsCount = getConfirmedRequests(eventId);

        RuntimeException validationError =
                participationRequestValidator.checkRequest(user.getId(), event, confirmedRequestsCount);

        if (validationError != null)
            throw validationError;

        ParticipationRequest request = new ParticipationRequest();
        request.setUserId(user.getId());
        request.setEvent(event);
        if (event.getParticipantLimit() == 0) {
            request.setStatus(ParticipationRequestStatus.CONFIRMED);
        } else {
            request.setStatus(event.isRequestModeration() ? ParticipationRequestStatus.PENDING : ParticipationRequestStatus.CONFIRMED);
        }
        request.setCreated(LocalDateTime.now());


        ParticipationRequest savedRequest = requestRepository.save(request);
        return ParticipationRequestToDtoMapper.mapToDto(savedRequest);
    }

    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.findByIdAndUserId(requestId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Request with id=" + requestId + " was not found"));

        request.setStatus(ParticipationRequestStatus.CANCELED);
        requestRepository.save(request);

        return ParticipationRequestToDtoMapper.mapToDto(request);
    }
}