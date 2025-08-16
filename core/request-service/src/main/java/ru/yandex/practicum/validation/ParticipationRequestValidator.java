package ru.yandex.practicum.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.events.EventFullDto;
import ru.yandex.practicum.enums.StateEvent;
import ru.yandex.practicum.errors.exceptions.EventOwnerParticipationException;
import ru.yandex.practicum.errors.exceptions.EventParticipationLimitException;
import ru.yandex.practicum.errors.exceptions.NotPublishedEventParticipationException;
import ru.yandex.practicum.errors.exceptions.RepeatParticipationRequestException;
import ru.yandex.practicum.repository.ParticipationRequestRepository;

@Component
@RequiredArgsConstructor
public class ParticipationRequestValidator {

    private final ParticipationRequestRepository requestRepository;

    public RuntimeException checkRequest(Long userId, EventFullDto event, long confirmedRequestsCount) {
        if (event.getInitiator().equals(userId)) {
            return new EventOwnerParticipationException("Event initiator cannot participate in their own event");
        }

        if (event.getState() != StateEvent.PUBLISHED) {
            return new NotPublishedEventParticipationException("Cannot participate in an unpublished event");
        }

        if (requestRepository.existsByUserIdAndEvent(userId, event.getId())) {
            return new RepeatParticipationRequestException("User already has a participation request for this event");
        }

        if (event.getParticipantLimit() > 0 && confirmedRequestsCount >= event.getParticipantLimit()) {
            return new EventParticipationLimitException("Event participant limit reached");
        }

        return null;
    }
}