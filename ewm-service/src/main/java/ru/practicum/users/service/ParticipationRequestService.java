package ru.practicum.users.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.events.model.Event;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.users.dto.ParticipationRequestDto;
import ru.practicum.users.mapper.ParticipationRequestToDtoMapper;
import ru.practicum.users.model.ParticipationRequest;
import ru.practicum.users.model.ParticipationRequestStatus;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.ParticipationRequestRepository;
import ru.practicum.users.validation.ParticipationRequestValidator;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestService {

    private final ParticipationRequestRepository requestRepository;
    private final EventRepository eventRepository;

    private final AdminUserService adminUserService;

    private final ParticipationRequestValidator participationRequestValidator;

    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        adminUserService.getUser(userId);
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
        User user = adminUserService.getUser(userId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id=" + eventId + " was not found"));
        long confirmedRequestsCount = getConfirmedRequests(eventId);

        RuntimeException validationError =
                participationRequestValidator.checkRequest(user, event, confirmedRequestsCount);

        if (validationError != null)
            throw validationError;

        ParticipationRequest request = new ParticipationRequest();
        request.setUser(user);
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