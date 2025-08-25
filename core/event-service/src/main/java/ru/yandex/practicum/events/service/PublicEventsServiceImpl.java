package ru.yandex.practicum.events.service;

import com.querydsl.core.BooleanBuilder;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.yandex.practicum.clients.UserFeignExceptionClient;
import ru.yandex.practicum.config.DateConfig;
import ru.yandex.practicum.controller.RecommendationsGrpcClient;
import ru.yandex.practicum.controller.UserActionGrpcClient;
import ru.yandex.practicum.dto.request.ParticipationRequestDto;
import ru.yandex.practicum.dto.user.UserActionDto;
import ru.yandex.practicum.dto.user.UserDto;
import ru.yandex.practicum.enums.ActionType;
import ru.yandex.practicum.errors.exceptions.AccessDeniedException;
import ru.yandex.practicum.events.client.RequestFeignDefaultClient;
import ru.yandex.practicum.events.client.RequestFeignExceptionClient;
import ru.yandex.practicum.events.dto.LookEventDto;
import ru.yandex.practicum.events.dto.SearchEventsParams;
import ru.yandex.practicum.dto.events.EventFullDto;
import ru.yandex.practicum.dto.events.EventShortDto;
import ru.yandex.practicum.enums.ParticipationRequestStatus;
import ru.yandex.practicum.enums.StateEvent;
import ru.yandex.practicum.errors.exceptions.EventNotPublishedException;
import ru.yandex.practicum.events.mapper.EventMapper;
import ru.yandex.practicum.events.model.Event;
import ru.yandex.practicum.events.model.QEvent;
import ru.yandex.practicum.events.repository.EventRepository;
import stats.service.dashboard.RecommendedEventProto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicEventsServiceImpl implements PublicEventsService {

    private final EventRepository eventRepository;

    private final UserActionGrpcClient userActionGrpcClient;

    private final RecommendationsGrpcClient recommendationsGrpcClient;

    private final UserFeignExceptionClient userFeignExceptionClient;

    private final RequestFeignDefaultClient requestFeignDefaultClient;

    private final RequestFeignExceptionClient requestFeignExceptionClient;

    @Override
    public Event getEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event with " + id + " not found"));
        event.setConfirmedRequests(
                requestFeignDefaultClient.getParticipationWithStatus(id, ParticipationRequestStatus.CONFIRMED));
        return event;
    }

    @Override
    public double getEventsRating(long id) {
        return recommendationsGrpcClient.getInteractionsCount(List.of(id))
                .findFirst()
                .map(RecommendedEventProto::getScore)
                .orElse(0.0);
    }

    @Override
    public EventFullDto getEventAnyStatusWithRating(Long id) {
        //Attention: this method works without saving views!
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event with " + id + " not found"));
        if (!event.getState().equals(StateEvent.PUBLISHED)) {
            throw new EventNotPublishedException("There is no published event id " + event.getId());
        }
        Integer confirmedCount = requestFeignDefaultClient
                .getParticipationWithStatus(id, ParticipationRequestStatus.CONFIRMED);
        event.setConfirmedRequests(confirmedCount != null ? confirmedCount : 0);
        event.setRating(getEventsRating(event.getId()));
        return EventMapper.toEventFullDto(event);
    }

    @Override
    public List<Event> getEventsByListIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids))
            return List.of();

        List<Event> events = eventRepository.findByIdIn(ids);
        if (CollectionUtils.isEmpty(events))
            return events;

        Map<Long, Long> counts = requestFeignExceptionClient.getParticipationCounts(ids);
        Map<Long, Double> recommendationsMap =
                recommendationsGrpcClient.getInteractionsCount(ids)
                        .collect(Collectors.toMap(
                                RecommendedEventProto::getEventId,
                                RecommendedEventProto::getScore
                        ));
        for (Event e : events) {
            e.setConfirmedRequests(Math.toIntExact(counts.getOrDefault(e.getId(), 0L)));
            e.setRating(recommendationsMap.getOrDefault(e.getId(), 0.0));
        }
        return events;
    }

    @Override
    public EventFullDto getEventInfo(LookEventDto lookEventDto) {
        log.info("\nPublicEventsServiceImpl.getEventInfo: accepted {}", lookEventDto);
        Event event = getEvent(lookEventDto.getId());
        log.info("\nPublicEventsServiceImpl.getEventsViews: event {}", event);
        if (!event.getState().equals(StateEvent.PUBLISHED)) {
            throw new EventNotPublishedException("There is no published event id " + event.getId());
        }

        UserActionDto userAction = UserActionDto.builder()
                .userId(lookEventDto.getUserId())
                .eventId(event.getId())
                .actionType(ActionType.VIEW)
                .timestamp(Instant.now())
                .build();

        userActionGrpcClient.sendUserAction(userAction);
        return EventMapper.toEventFullDto(event);
    }

    @Override
    public List<EventShortDto> getFilteredEvents(SearchEventsParams searchEventsParams, LookEventDto lookEventDto) {
        log.info("\nPublicEventsServiceImpl.getFilteredEvents: {}", searchEventsParams);

        BooleanBuilder builder = new BooleanBuilder();

        // Добавляем условия отбора по контексту
        if (!Strings.isEmpty(searchEventsParams.getText())) {
            builder.or(QEvent.event.annotation.containsIgnoreCase(searchEventsParams.getText()))
                    .or(QEvent.event.description.containsIgnoreCase(searchEventsParams.getText()));
        }

        // Добавляем отбор по статусу PUBLISHED
        builder.and(QEvent.event.state.eq(StateEvent.PUBLISHED));
        // ... и по списку категорий
        if (!CollectionUtils.isEmpty(searchEventsParams.getCategories()))
            builder.and(QEvent.event.category.in(searchEventsParams.getCategories()));

        // ... и еще по признаку платные/бесплатные
        if (searchEventsParams.getPaid() != null)
            builder.and(QEvent.event.paid.eq(searchEventsParams.getPaid()));

        // Добавляем условие диапазона дат
        LocalDateTime start;
        LocalDateTime end;
        if (searchEventsParams.getRangeStart() == null) {
            start = LocalDateTime.now();
            searchEventsParams.setRangeStart(start.format(DateConfig.FORMATTER));
        } else {
            start = LocalDateTime.parse(searchEventsParams.getRangeStart(), DateConfig.FORMATTER);
        }
        if (searchEventsParams.getRangeEnd() == null) {
            builder.and(QEvent.event.eventDate.goe(start));
        } else {
            end = LocalDateTime.parse(searchEventsParams.getRangeEnd(), DateConfig.FORMATTER);
            builder.and(QEvent.event.eventDate.between(start, end));
        }

        int page = searchEventsParams.getFrom() / searchEventsParams.getSize();
        int size = searchEventsParams.getSize();
        List<Event> events = eventRepository.findAll(builder, PageRequest.of(page, size)).getContent();

        if (events.isEmpty()) {
            return List.of();
        }
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();
        Map<Long, Long> counts = requestFeignDefaultClient.getParticipationCounts(eventIds);

        for (Event e : events) {
            e.setConfirmedRequests(Math.toIntExact(counts.getOrDefault(e.getId(), 0L)));
        }
        if (searchEventsParams.getOnlyAvailable()) {
            events = events.stream()
                    .filter(ev -> ev.getParticipantLimit() == 0 || ev.getParticipantLimit() > ev.getConfirmedRequests())
                    .collect(Collectors.toList());
        }
        log.info("PublicEventsServiceImpl.getFilteredEvents: events {}", events);
        Map<Long, Double> recommendationsMap =
                recommendationsGrpcClient.getInteractionsCount(eventIds)
                        .collect(Collectors.toMap(
                                RecommendedEventProto::getEventId,
                                RecommendedEventProto::getScore
                        ));
        for (Event e : events) {
            e.setRating(recommendationsMap.getOrDefault(e.getId(), 0.0));
        }
        // Если не было установлено rangeEnd, устанавливаем
        if (searchEventsParams.getRangeEnd() == null) {
            searchEventsParams.setRangeEnd(LocalDateTime.now().format(DateConfig.FORMATTER));
        }
        // Сортировка. Для начала проверяем значение параметра сортировки
        String sortParam;
        if (Strings.isEmpty(searchEventsParams.getSort())) {
            sortParam = "RATING";
        } else {
            sortParam = searchEventsParams.getSort().toUpperCase();
        }
        // Дополняем сортировкой
        List<Event> sortedEvents;
        if (sortParam.equalsIgnoreCase("EVENT_DATE")) {
            sortedEvents = events.stream()
                    .sorted(Comparator.comparing(Event::getEventDate)) // Сортируем по eventDate
                    .toList();
        } else {
            sortedEvents = events.stream()
                    .sorted(Comparator.comparingDouble(Event::getRating).reversed()) // Сортируем по rating
                    .toList();
        }
        log.info("\n Final list {}", sortedEvents);
        return EventMapper.toListEventShortDto(sortedEvents);
    }

    @Override
    public List<EventShortDto> getRecommendations4User(Long userId, int quant) {
        Map<Long, Double> recommendationsMap =
                recommendationsGrpcClient.getRecommendationsForUser(userId, quant)
                        .collect(Collectors.toMap(
                                RecommendedEventProto::getEventId,
                                RecommendedEventProto::getScore
                        ));
        List<Event> events = eventRepository.findByIdIn(
                new ArrayList<>(recommendationsMap.keySet())
        );
        events.forEach(e -> e.setRating(recommendationsMap.get(e.getId())));
        events.sort(Comparator.comparingDouble(Event::getRating).reversed());
        return EventMapper.toListEventShortDto(events);
    }

    @Override
    public void putUsersLike(Long eventId, Long userId) {
        UserDto user = userFeignExceptionClient.getUserById(userId);
        Event event = getEvent(eventId);
        if (event.getEventDate().isAfter(LocalDateTime.now())) {
            throw new AccessDeniedException("Like before date of event is prohibited");
        }
        List<ParticipationRequestDto> requests = requestFeignDefaultClient.getRequestList(eventId);
        ParticipationRequestDto dto = requests.stream()
                .filter(p -> p.getRequester().equals(userId))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("User " + userId + "was not a participant - operation is prohibited"));

        if (dto.getStatus() != ParticipationRequestStatus.CONFIRMED) {
            throw new IllegalArgumentException("User " + userId + "was not a participant - operation is prohibited");
        }
        UserActionDto userActionDto = UserActionDto.builder()
                .userId(userId)
                .eventId(eventId)
                .actionType(ActionType.LIKE)
                .timestamp(Instant.now())
                .build();
        userActionGrpcClient.sendUserAction(userActionDto);
    }

}