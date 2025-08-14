package ru.yandex.practicum.events.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.category.CategoryDto;

@Service
@RequiredArgsConstructor
public class EventCategoryExceptionClient {

    private final EventCategoryFeign eventCategoryFeign;

    @CircuitBreaker(name = "categoryService", fallbackMethod = "getInfoByIdFallback")
    public CategoryDto getInfoById(Long catId) {
        return eventCategoryFeign.getInfoById(catId).getBody();
    }

    public CategoryDto getInfoByIdFallback(Long catId, Throwable t) {
        throw new EntityNotFoundException("Category with " + catId + " not found");
    }
}