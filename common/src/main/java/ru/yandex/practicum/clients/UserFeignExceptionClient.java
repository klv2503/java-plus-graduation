package ru.yandex.practicum.clients;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.user.UserDto;
import ru.yandex.practicum.errors.exceptions.AccessDeniedException;

@Service
@RequiredArgsConstructor
public class UserFeignExceptionClient {

    private final UserServiceFeign userServiceFeign;

    @CircuitBreaker(name = "categoryService", fallbackMethod = "getInfoByIdFallback")
    public UserDto getUserById(Long userId) {
        return userServiceFeign.getUserById(userId).getBody();
    }

    public UserDto getInfoByIdFallback(Long userId, Throwable t) {
        throw new AccessDeniedException("Operation is temporally not allowed.");
    }
}