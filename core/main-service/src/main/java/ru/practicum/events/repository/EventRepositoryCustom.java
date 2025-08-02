package ru.practicum.events.repository;

import com.querydsl.core.BooleanBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.events.model.Event;
import ru.practicum.users.model.ParticipationRequestStatus;

import java.util.List;

public interface EventRepositoryCustom {
    Page<Event> findAllWithBuilder(BooleanBuilder builder, Pageable pageable);

    Event findEventWithStatus(Long eventId, ParticipationRequestStatus status);

    List<Event> searchEvents(BooleanBuilder eventCondition, ParticipationRequestStatus status,
                             boolean isAvailable, int page, int size);

    List<Event> findEventsWithConfirmedCount(List<Long> eventIds);

    Event getSingleEvent(Long id);

}