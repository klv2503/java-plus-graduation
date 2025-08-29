package ru.yandex.practicum.events.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.category.CategoryDto;
import ru.yandex.practicum.errors.exceptions.AccessDeniedException;

@Service
@RequiredArgsConstructor
public class EventCategoryExceptionClient {

    private final EventCategoryFeign eventCategoryFeign;

    @CircuitBreaker(name = "categoryService", fallbackMethod = "getInfoByIdFallback")
    public CategoryDto getInfoById(Long catId) {
        return eventCategoryFeign.getInfoById(catId).getBody();
    }

    public CategoryDto getInfoByIdFallback(Long catId, Throwable t) {
        throw new AccessDeniedException("Operation is temporally not allowed.");
    }
}