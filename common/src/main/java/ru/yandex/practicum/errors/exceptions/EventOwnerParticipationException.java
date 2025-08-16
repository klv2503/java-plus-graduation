package ru.yandex.practicum.errors.exceptions;

import org.springframework.dao.DataIntegrityViolationException;

public class EventOwnerParticipationException extends DataIntegrityViolationException {
    public EventOwnerParticipationException(String message) {
        super(message);
    }
}
