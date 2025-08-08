package ru.yandex.practicum.events.validation;

import ru.yandex.practicum.errors.ForbiddenActionException;
import ru.yandex.practicum.events.dto.UpdateEventAdminRequest;
import ru.yandex.practicum.events.model.Event;
import ru.yandex.practicum.events.model.EventStateAction;
import ru.yandex.practicum.events.model.StateEvent;

import java.time.LocalDateTime;

public class AdminEventValidator {
    public static void validateEventStatusUpdate(Event event, UpdateEventAdminRequest updateRequest) {
        LocalDateTime eventDate = updateRequest.getEventDate();

        if (updateRequest.getEventDate() != null && eventDate.isBefore(event.getCreatedOn().plusHours(1))) {
            throw new ForbiddenActionException("Event start time must be at least 1 hour after publication.");
        }

        if (updateRequest.getStateAction() == null)
            return;

        boolean isPublishAction = updateRequest.getStateAction()
                .equals(EventStateAction.PUBLISH_EVENT);
        boolean isRejectAction = updateRequest.getStateAction()
                .equals(EventStateAction.REJECT_EVENT);

        if (isPublishAction && !event.getState().equals(StateEvent.PENDING)) {
            throw new ForbiddenActionException("Cannot publish event. It must be in PENDING state.");
        }

        if (isRejectAction && event.getState().equals(StateEvent.PUBLISHED)) {
            throw new ForbiddenActionException("Cannot reject event. It is already published.");
        }
    }
}
