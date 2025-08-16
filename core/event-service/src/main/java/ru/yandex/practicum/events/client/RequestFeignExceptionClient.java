package ru.yandex.practicum.events.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RequestFeignExceptionClient {

    private final EventRequestFeign eventRequestFeign;

    @CircuitBreaker(name = "requestService", fallbackMethod = "getParticipationCountsFallback")
    public Map<Long, Long> getParticipationCounts(List<Long> ids) {
        return eventRequestFeign.getParticipationCounts(ids).getBody();
    }

    public Map<Long, Long> getParticipationCountsFallback(List<Long> ids, Throwable t) {
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Participation count service is currently unavailable"
        );
    }

}