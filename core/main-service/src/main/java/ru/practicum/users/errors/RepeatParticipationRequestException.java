package ru.practicum.users.errors;

import org.springframework.dao.DataIntegrityViolationException;

public class RepeatParticipationRequestException extends DataIntegrityViolationException {
    public RepeatParticipationRequestException(String message) {
        super(message);
    }
}
