package ru.yandex.practicum.errors.exceptions;

import org.springframework.dao.DataIntegrityViolationException;

public class EventParticipationLimitException extends DataIntegrityViolationException {
    public EventParticipationLimitException(String message) {
        super(message);
    }
}
