package ru.practicum.users.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.events.model.Event;
import ru.practicum.events.model.StateEvent;
import ru.practicum.users.errors.EventOwnerParticipationException;
import ru.practicum.users.errors.EventParticipationLimitException;
import ru.practicum.users.errors.NotPublishedEventParticipationException;
import ru.practicum.users.errors.RepeatParticipationRequestException;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.ParticipationRequestRepository;

@Component
@RequiredArgsConstructor
public class ParticipationRequestValidator {

    private final ParticipationRequestRepository requestRepository;

    public RuntimeException checkRequest(User user, Event event, long confirmedRequestsCount) {
        if (event.getInitiator().getId().equals(user.getId())) {
            return new EventOwnerParticipationException("Event initiator cannot participate in their own event");
        }

        if (event.getState() != StateEvent.PUBLISHED) {
            return new NotPublishedEventParticipationException("Cannot participate in an unpublished event");
        }

        if (requestRepository.existsByUserIdAndEventId(user.getId(), event.getId())) {
            return new RepeatParticipationRequestException("User already has a participation request for this event");
        }

        if (event.getParticipantLimit() > 0 && confirmedRequestsCount >= event.getParticipantLimit()) {
            return new EventParticipationLimitException("Event participant limit reached");
        }

        return null;
    }
}