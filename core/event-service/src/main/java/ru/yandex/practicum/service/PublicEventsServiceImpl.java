package ru.yandex.practicum.service;

import com.querydsl.core.BooleanBuilder;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.yandex.practicum.client.EventRequestFeign;
import ru.yandex.practicum.config.DateConfig;
import ru.yandex.practicum.controller.ClientController;
import ru.yandex.practicum.dto.LookEventDto;
import ru.yandex.practicum.dto.SearchEventsParams;
import ru.yandex.practicum.dto.endpoint.ReadEndpointHitDto;
import ru.yandex.practicum.dto.events.EventFullDto;
import ru.yandex.practicum.dto.events.EventShortDto;
import ru.yandex.practicum.enums.ParticipationRequestStatus;
import ru.yandex.practicum.enums.StateEvent;
import ru.yandex.practicum.errors.exceptions.EventNotPublishedException;
import ru.yandex.practicum.mapper.EventMapper;
import ru.yandex.practicum.model.Event;
import ru.yandex.practicum.model.QEvent;
import ru.yandex.practicum.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicEventsServiceImpl implements PublicEventsService {

    private final EventRepository eventRepository;

    private final ClientController clientController;

    private final EventRequestFeign eventRequestFeign;

    @Override
    public Event getEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event with " + id + " not found"));
        event.setConfirmedRequests(
                eventRequestFeign.getParticipationWithStatus(id, ParticipationRequestStatus.CONFIRMED)
                        .getBody());
        return event;
    }

    @Override
    public int getEventsViews(long id, LocalDateTime publishedOn) {
        List<String> uris = List.of("/events/" + id);
        List<ReadEndpointHitDto> res = clientController.getHits(publishedOn.format(DateConfig.FORMATTER),
                LocalDateTime.now().format(DateConfig.FORMATTER), uris, true);
        log.info("\nPublicEventsServiceImpl.getEventsViews: res {}", res);
        return (CollectionUtils.isEmpty(res)) ? 0 : res.getFirst().getHits();
    }

    @Override
    public EventFullDto getEventAnyStatusWithViews(Long id) {
        //Attention: this method works without saving views!
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event with " + id + " not found"));
        if (!event.getState().equals(StateEvent.PUBLISHED)) {
            throw new EventNotPublishedException("There is no published event id " + event.getId());
        }
        Integer confirmedCount = eventRequestFeign
                .getParticipationWithStatus(id, ParticipationRequestStatus.CONFIRMED).getBody();
        event.setConfirmedRequests(confirmedCount != null ? confirmedCount : 0);
        event.setViews(getEventsViews(event.getId(), event.getPublishedOn()));
        return EventMapper.toEventFullDto(event);
    }

    @Override
    public List<Event> getEventsByListIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids))
            return List.of();

        List<Event> events = eventRepository.findByIdIn(ids);
        if (CollectionUtils.isEmpty(events))
            return events;

        Map<Long, Long> counts = eventRequestFeign.getParticipationCounts(ids).getBody();
        for (Event e : events) {
            e.setConfirmedRequests(Math.toIntExact(counts.getOrDefault(e.getId(), 0L)));
        }
        LocalDateTime start = events.stream()
                .map(Event::getPublishedOn)
                .min(LocalDateTime::compareTo)
                .orElseThrow(() ->
                        new RuntimeException("Internal server error during execution PublicEventsServiceImpl"));
        List<String> uris = events.stream()
                .map(event -> "/event/" + event.getId())
                .toList();

        List<ReadEndpointHitDto> acceptedList = clientController.getHits(start.format(DateConfig.FORMATTER),
                LocalDateTime.now().format(DateConfig.FORMATTER), uris, true);
        // Заносим значения views в список events
        viewsToEvents(acceptedList, events);
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
        // Получаем views
        event.setViews(getEventsViews(event.getId(), event.getPublishedOn()));
        //Имеем новый просмотр - сохраняем его
        clientController.saveView(lookEventDto.getIp(), lookEventDto.getUri());

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
            clientController.saveView(lookEventDto.getIp(), "/events");
            return List.of();
        }
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();
        Map<Long, Long> counts = eventRequestFeign.getParticipationCounts(eventIds).getBody();
        for (Event e : events) {
            e.setConfirmedRequests(Math.toIntExact(counts.getOrDefault(e.getId(), 0L)));
        }
        if (searchEventsParams.getOnlyAvailable()) {
            events = events.stream()
                    .filter(ev -> ev.getParticipantLimit() == 0 || ev.getParticipantLimit() > ev.getConfirmedRequests())
                    .collect(Collectors.toList());
        }
        log.info("PublicEventsServiceImpl.getFilteredEvents: events {}", events);
        // Если не было установлено rangeEnd, устанавливаем
        if (searchEventsParams.getRangeEnd() == null) {
            searchEventsParams.setRangeEnd(LocalDateTime.now().format(DateConfig.FORMATTER));
        }
        // Формируем список uris
        List<String> uris = new ArrayList<>();
        for (Event e : events) {
            uris.add("/events/" + e.getId());
        }

        List<ReadEndpointHitDto> acceptedList = clientController.getHits(searchEventsParams.getRangeStart(),
                searchEventsParams.getRangeEnd(), uris, true);
        viewsToEvents(acceptedList, events);

        // Сортировка. Для начала проверяем значение параметра сортировки
        String sortParam;
        if (Strings.isEmpty(searchEventsParams.getSort())) {
            sortParam = "VIEWS";
        } else {
            sortParam = searchEventsParams.getSort().toUpperCase();
        }
        // Дополняем сортировкой
        List<Event> sortedEvents = new ArrayList<>();
        if (sortParam.equalsIgnoreCase("EVENT_DATE")) {
            sortedEvents = events.stream()
                    .sorted(Comparator.comparing(Event::getEventDate)) // Сортируем по eventDate
                    .toList();
        } else {
            sortedEvents = events.stream()
                    .sorted(Comparator.comparingInt(Event::getViews).reversed()) // Сортируем по views
                    .toList();
        }

        uris.add("/events");
        clientController.saveHitsGroup(uris, lookEventDto.getIp());
        log.info("\n Final list {}", sortedEvents);
        return EventMapper.toListEventShortDto(sortedEvents);
    }

    public void viewsToEvents(List<ReadEndpointHitDto> viewsList, List<Event> events) {
        // Заносим значения views в список events
        Map<Integer, Integer> workMap = new HashMap<>();
        for (ReadEndpointHitDto r : viewsList) {
            int i = Integer.parseInt(r.getUri().substring(r.getUri().lastIndexOf("/") + 1));
            workMap.put(i, r.getHits());
        }
        for (Event e : events) {
            e.setViews(workMap.getOrDefault(e.getId(), 0));
        }
    }
}