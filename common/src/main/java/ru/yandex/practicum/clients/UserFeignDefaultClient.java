package ru.yandex.practicum.clients;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.user.UserDto;

@Service
@RequiredArgsConstructor
public class UserFeignDefaultClient {

    private final UserServiceFeign userServiceFeign;

    @CircuitBreaker(name = "categoryService", fallbackMethod = "getInfoByIdFallback")
    public UserDto getUserById(Long userId) {
        return userServiceFeign.getUserById(userId).getBody();
    }

    public UserDto getInfoByIdFallback(Long userId, Throwable t) {
        return UserDto.builder()
                .id(10000000000L)
                .build();
    }
}