package ru.yandex.practicum.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.clients.*;
import ru.yandex.practicum.dto.request.ChangeRequestStatus;
import ru.yandex.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.yandex.practicum.dto.request.ParticipationRequestDto;
import ru.yandex.practicum.dto.events.EventFullDto;
import ru.yandex.practicum.dto.user.UserDto;
import ru.yandex.practicum.enums.ParticipationRequestStatus;
import ru.yandex.practicum.errors.exceptions.ForbiddenActionException;
import ru.yandex.practicum.mapper.ParticipationRequestToDtoMapper;
import ru.yandex.practicum.model.ParticipationRequest;
import ru.yandex.practicum.repository.ParticipationRequestRepository;
import ru.yandex.practicum.validation.ParticipationRequestValidator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ParticipationRequestService {

    private final ParticipationRequestRepository requestRepository;

    private final UserFeignDefaultClient userFeignDefaultClient;

    private final UserFeignExceptionClient userFeignExceptionClient;

    private final EventFeignExceptionClient eventFeignExceptionClient;

    private final ParticipationRequestValidator participationRequestValidator;

    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        userFeignDefaultClient.getUserById(userId);
        return requestRepository.findByUserId(userId)
                .stream()
                .map(ParticipationRequestToDtoMapper::mapToDto)
                .toList();
    }

    public int getConfirmedRequests(long eventId) {
        return requestRepository
                .countConfirmedRequestsByStatusAndEvent(ParticipationRequestStatus.CONFIRMED, eventId);
    }

    @Transactional
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        UserDto user = userFeignExceptionClient.getUserById(userId);
        EventFullDto event = eventFeignExceptionClient.getEventInfo(eventId);
        long confirmedRequestsCount = getConfirmedRequests(eventId);
        log.info("\n✅addParticipationRequest: accepted event{}, count {}", event, confirmedRequestsCount);

        RuntimeException validationError =
                participationRequestValidator.checkRequest(user.getId(), event, confirmedRequestsCount);

        if (validationError != null)
            throw validationError;

        ParticipationRequest request = new ParticipationRequest();
        request.setUserId(user.getId());
        request.setEvent(event.getId());
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

    public int getParticipationWithStatus(Long eventId, ParticipationRequestStatus status) {
        return requestRepository.countConfirmedRequestsByStatusAndEvent(status, eventId);
    }

    public Map<Long, Long> getParticipationCounts(List<Long> eventIds) {
        List<Object[]> results = requestRepository.countByEventIds(eventIds);

        return results.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }

    public List<ParticipationRequestDto> getRequestList(Long eventId) {
        return requestRepository.findByEvent(eventId).stream()
                .map(ParticipationRequestToDtoMapper::mapToDto)
                .toList();
    }

    @Transactional
    public EventRequestStatusUpdateResult changeRequestStatuses(ChangeRequestStatus change) {
        List<ParticipationRequest> participation = requestRepository.findByIds(change.getRequestIds());
        for (ParticipationRequest req : participation) {
            if (!req.getStatus().equals(ParticipationRequestStatus.PENDING)) {
                throw new ForbiddenActionException("All requests must be PENDING status, but request with id"
                        + req.getId() + " has status " + req.getStatus());
            }
        }
        int partLimit = change.getLimit();
        int confPart = getParticipationWithStatus(change.getEvent(), ParticipationRequestStatus.CONFIRMED);
        int diff = partLimit - confPart;
        if (diff >= change.getRequestIds().size()) {
            requestRepository.updateStatusByIds(change.getStatus(), change.getRequestIds());
            if (change.getStatus() == ParticipationRequestStatus.CONFIRMED) {

                for (ParticipationRequest req : participation) {
                    req.setStatus(ParticipationRequestStatus.CONFIRMED);
                }

                return EventRequestStatusUpdateResult.builder()
                        .confirmedRequests(participation.stream()
                                .map(ParticipationRequestToDtoMapper::mapToDto).toList())
                        .build();
            } else {
                for (ParticipationRequest req : participation) {
                    req.setStatus(ParticipationRequestStatus.REJECTED);
                }

                return EventRequestStatusUpdateResult.builder()
                        .rejectedRequests(participation.stream()
                                .map(ParticipationRequestToDtoMapper::mapToDto).toList())
                        .build();
            }
        } else if (diff == 0) {
            throw new ForbiddenActionException("Participation limit is 0");
        } else {
            List<Long> confirmed = new ArrayList<>();
            List<Long> rejected = new ArrayList<>();
            for (int i = 1; i <= change.getRequestIds().size(); i++) {
                if (i > diff) {
                    rejected.add(change.getRequestIds().get(i));
                } else confirmed.add(change.getRequestIds().get(i));
            }
            requestRepository.updateStatusByIds(ParticipationRequestStatus.CONFIRMED, confirmed);
            requestRepository.updateStatusByIds(ParticipationRequestStatus.REJECTED, rejected);

            EventRequestStatusUpdateResult res = new EventRequestStatusUpdateResult();

            for (ParticipationRequest req : participation) {
                for (Long id : confirmed) {
                    if (Objects.equals(req.getId(), id)) {
                        req.setStatus(ParticipationRequestStatus.CONFIRMED);
                    }
                }
                req.setStatus(ParticipationRequestStatus.CANCELED);
            }

            List<ParticipationRequest> updatedRequestsConfirmed = participation.stream()
                    .filter(req -> confirmed.contains(req.getId()))
                    .peek(req -> req.setStatus(ParticipationRequestStatus.CONFIRMED))
                    .toList();

            List<ParticipationRequest> updatedRequestsRejected = participation.stream()
                    .filter(req -> rejected.contains(req.getId()))
                    .peek(req -> req.setStatus(ParticipationRequestStatus.REJECTED))
                    .toList();

            res.setConfirmedRequests(updatedRequestsConfirmed.stream()
                    .map(ParticipationRequestToDtoMapper::mapToDto).toList());
            res.setRejectedRequests(updatedRequestsRejected.stream()
                    .map(ParticipationRequestToDtoMapper::mapToDto).toList());

            return res;
        }
    }
}