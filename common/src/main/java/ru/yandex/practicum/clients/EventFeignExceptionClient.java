package ru.yandex.practicum.clients;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.events.EventFullDto;
import ru.yandex.practicum.errors.exceptions.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventFeignExceptionClient {

    private final EventServiceFeign eventServiceFeign;

    @CircuitBreaker(name = "categoryService", fallbackMethod = "getEventInfoFallback")
    public EventFullDto getEventInfo(Long eventId) {
        return eventServiceFeign.getEventInfo(eventId).getBody();
    }

    public EventFullDto getEventInfoFallback(Long eventId, Throwable t) {
        throw new AccessDeniedException("Operation temporally not allowed");
    }

    @CircuitBreaker(name = "categoryService", fallbackMethod = "getEventsFallback")
    public List<EventFullDto> getEvents(List<Long> users, List<String> states, List<Long> categories,
            LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        return eventServiceFeign.getEvents(users, states, categories, rangeStart, rangeEnd, from, size).getBody();
    }

    public List<EventFullDto> getEventsFallback(List<Long> users, List<String> states, List<Long> categories,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size, Throwable t) {
        throw new AccessDeniedException("Operation is currently not allowed.");
    }
}