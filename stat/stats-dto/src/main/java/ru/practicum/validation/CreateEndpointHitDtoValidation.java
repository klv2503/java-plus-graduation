package ru.practicum.validation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public interface CreateEndpointHitDtoValidation {

    @NotBlank(message = "App identifier is required")
    String getApp();

    @NotBlank(message = "URI is required")
    String getUri();

    @NotBlank(message = "IP address is required")
    String getIp();

    @NotNull(message = "Timestamp is required")
    LocalDateTime getTimestamp();
}

