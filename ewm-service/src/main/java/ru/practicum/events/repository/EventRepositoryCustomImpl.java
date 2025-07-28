package ru.practicum.events.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import ru.practicum.events.model.Event;
import ru.practicum.events.model.QEvent;
import ru.practicum.users.model.ParticipationRequestStatus;
import ru.practicum.users.model.QParticipationRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EventRepositoryCustomImpl implements EventRepositoryCustom {
    private final EntityManager em;

    @Override
    public Page<Event> findAllWithBuilder(BooleanBuilder builder, Pageable pageable) {
        QEvent event = QEvent.event;

        // Запрос для получения данных с пагинацией и сортировкой
        JPAQuery<Event> query = new JPAQuery<>(em)
                .select(event)
                .from(event)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());
        List<Event> content = query.fetch();

        // Запрос для подсчета общего количества элементов (используем count)
        Long total = new JPAQuery<>(em).select(event.count())
                .from(event)
                .fetchOne();

        if (total == null)
            throw new RuntimeException("Не удалось определить количество событий");

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Event findEventWithStatus(Long eventId, ParticipationRequestStatus status) {
        QEvent event = QEvent.event;
        QParticipationRequest request = QParticipationRequest.participationRequest;

        JPAQuery<Tuple> query = new JPAQuery<>(em)
                .select(event, request.id.count())
                .from(event)
                .leftJoin(request).on(request.event.eq(event).and(request.status.eq(status)))
                .where(event.id.eq(eventId))
                .groupBy(event);
        Tuple result = query.fetchOne();

        if (result == null) {
            throw new EntityNotFoundException("Event with" + eventId + " not found");
        }

        Event foundEvent = result.get(event);
        Integer confirmedCount = result.get(request.id.count()).intValue();

        foundEvent.setConfirmedRequests((confirmedCount == null) ? 0 : confirmedCount);

        return foundEvent;
    }

    @Override
    public List<Event> searchEvents(BooleanBuilder eventCondition, ParticipationRequestStatus status,
                                    boolean onlyAvailable, int from, int size) {
        QEvent event = QEvent.event;
        QParticipationRequest participation = QParticipationRequest.participationRequest;

        // Строим запрос
        JPAQuery<Tuple> query = new JPAQuery<>(em)
                .select(event, participation.count())
                .from(event)
                .leftJoin(participation).on(participation.event.id.eq(event.id)
                        .and(participation.status.eq(status)))
                .where(eventCondition)
                .groupBy(event.id);

        // Выполняем запрос
        List<Tuple> results = query.fetch();

        // обработка результата
        List<Event> events = new ArrayList<>();
        if (onlyAvailable) {
            events = tuplesToEvents(event, results).stream()
                    .filter(ev -> ev.getParticipantLimit() == 0 ||
                            ev.getParticipantLimit() > ev.getConfirmedRequests())
                    .toList();
        } else {
            events = tuplesToEvents(event, results);
        }

        int toIndex = Math.min(from + size, events.size());
        if (from >= events.size()) {
            return List.of();
        }
        return events.subList(from, toIndex);
    }

    @Override
    public List<Event> findEventsWithConfirmedCount(List<Long> eventIds) {
        QEvent event = QEvent.event;
        QParticipationRequest participation = QParticipationRequest.participationRequest;

        JPAQuery<Tuple> query = new JPAQuery<>(em)
                .select(event, participation.count())
                .from(event)
                .leftJoin(participation)
                .on(participation.event.id.eq(event.id)
                        .and(participation.status.eq(ParticipationRequestStatus.CONFIRMED)))
                .where(event.id.in(eventIds))
                .groupBy(event.id);

        List<Tuple> currentList = query.fetch();
        return tuplesToEvents(event, currentList);
    }

    @Override
    public Event getSingleEvent(Long id) {
        QEvent event = QEvent.event;
        QParticipationRequest participation = QParticipationRequest.participationRequest;

        Tuple result = new JPAQuery<>(em)
                .select(event, participation.id.count().coalesce(0L)) // Берем event и количество подтвержденных заявок
                .from(event)
                .leftJoin(participation).on(event.id.eq(participation.event.id)
                        .and(participation.status.eq(ParticipationRequestStatus.CONFIRMED)))
                .where(event.id.eq(id))
                .groupBy(event.id)
                .fetchOne();

        if (result == null || result.get(event) == null) {
            return null;
        }

        Event eventResult = result.get(event);
        if (eventResult == null) {
            return null;
        }
        Integer confirmedRequestsCount = result.get(participation.id.count().coalesce(0L).intValue());

        eventResult.setConfirmedRequests((confirmedRequestsCount == null) ? 0 : confirmedRequestsCount);

        return eventResult;
    }

    private List<Event> tuplesToEvents(QEvent event, List<Tuple> tuples) {
        List<Event> events = new ArrayList<>();
        for (Tuple tuple : tuples) {
            Event e = tuple.get(event);  // Извлекаем событие
            if (e != null) {
                Integer confirmedCount = Optional.ofNullable(tuple.get(1, Long.class))
                        .map(Long::intValue)
                        .orElse(0);  // Извлекаем количество участников
                e.setConfirmedRequests(confirmedCount);  // пишем в транзиентное поле
                events.add(e);
            }
        }
        return events;
    }
}
