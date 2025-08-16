package ru.yandex.practicum.events.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.request.ChangeRequestStatus;
import ru.yandex.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.yandex.practicum.dto.request.ParticipationRequestDto;
import ru.yandex.practicum.enums.ParticipationRequestStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RequestFeignDefaultClient {

    private final EventRequestFeign eventRequestFeign;

    @CircuitBreaker(name = "requestService", fallbackMethod = "changeRequestStatusesFallback")
    public EventRequestStatusUpdateResult changeRequestStatuses(ChangeRequestStatus changeRequestStatus) {
        return eventRequestFeign.changeRequestStatuses(changeRequestStatus).getBody();
    }

    public EventRequestStatusUpdateResult changeRequestStatusesFallback(Long userId, Throwable t) {
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(List.of())
                .rejectedRequests(List.of())
                .build();
    }

    @CircuitBreaker(name = "requestService", fallbackMethod = "getRequestListFallback")
    public List<ParticipationRequestDto> getRequestList(Long eventId) {
        return eventRequestFeign.getRequestList(eventId).getBody();
    }

    public List<ParticipationRequestDto> getRequestListFallback(Long eventId, Throwable t) {
        return List.of();
    }

    @CircuitBreaker(name = "requestService", fallbackMethod = "getParticipationWithStatusFallback")
    public Integer getParticipationWithStatus(Long eventId, ParticipationRequestStatus status) {
        return eventRequestFeign.getParticipationWithStatus(eventId, status).getBody();
    }

    public Integer getParticipationWithStatusFallback(Long eventId, ParticipationRequestStatus status, Throwable t) {
        return 0;
    }

    @CircuitBreaker(name = "requestService", fallbackMethod = "getParticipationCountsFallback")
    public Map<Long, Long> getParticipationCounts(List<Long> ids) {
        return eventRequestFeign.getParticipationCounts(ids).getBody();
    }

    public Map<Long, Long> getParticipationCountsFallback(List<Long> ids, Throwable t) {
        return new HashMap<>();
    }
}