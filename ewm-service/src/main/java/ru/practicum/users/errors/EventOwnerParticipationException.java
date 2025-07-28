package ru.practicum.users.errors;

import org.springframework.dao.DataIntegrityViolationException;

public class EventOwnerParticipationException extends DataIntegrityViolationException {
    public EventOwnerParticipationException(String message) {
        super(message);
    }
}
