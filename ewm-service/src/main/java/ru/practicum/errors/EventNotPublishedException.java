package ru.practicum.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EventNotPublishedException extends RuntimeException {
    public EventNotPublishedException(String message) {
        super(message);
    }
}
