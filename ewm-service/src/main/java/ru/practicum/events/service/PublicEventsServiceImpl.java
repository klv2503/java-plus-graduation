package ru.practicum.events.service;

import com.querydsl.core.BooleanBuilder;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.practicum.config.DateConfig;
import ru.practicum.config.StatsClientConfig;
import ru.practicum.controller.ClientController;
import ru.practicum.dto.ReadEndpointHitDto;
import ru.practicum.errors.EventNotPublishedException;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.dto.LookEventDto;
import ru.practicum.events.dto.SearchEventsParams;
import ru.practicum.events.mapper.EventMapper;
import ru.practicum.events.model.Event;
import ru.practicum.events.model.QEvent;
import ru.practicum.events.model.StateEvent;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.users.model.ParticipationRequestStatus;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicEventsServiceImpl implements PublicEventsService {

    private final EventRepository eventRepository;

    private final ClientController clientController;

    @Autowired
    public PublicEventsServiceImpl(EventRepository eventRepository, StatsClientConfig statsClientConfig) {
        this.eventRepository = eventRepository;
        this.clientController = new ClientController(statsClientConfig.getHost(), statsClientConfig.getPort());
    }

    @Override
    public Event getEvent(Long id) {
        return eventRepository.findEventWithStatus(id, ParticipationRequestStatus.CONFIRMED);
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
    public Event getEventAnyStatusWithViews(Long id) {
        //Attention: this method works without saving views!
        Event event = eventRepository.getSingleEvent(id);
        if (event == null) {
                throw new EntityNotFoundException("Event with " + id + " not found");
        }
        if (!event.getState().equals(StateEvent.PUBLISHED)) {
            throw new EventNotPublishedException("There is no published event id " + event.getId());
        }
        event.setViews(getEventsViews(event.getId(), event.getPublishedOn()));
        return event;
    }

    public List<Event> getEventsByListIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids))
            return List.of();

        List<Event> events = eventRepository.findEventsWithConfirmedCount(ids);
        if (CollectionUtils.isEmpty(events))
            return events;

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
            builder.and(QEvent.event.category.id.in(searchEventsParams.getCategories()));

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

        List<Event> events = eventRepository.searchEvents(builder, ParticipationRequestStatus.CONFIRMED,
                searchEventsParams.getOnlyAvailable(), searchEventsParams.getFrom(), searchEventsParams.getSize());
        if (events.isEmpty()) {
            clientController.saveView(lookEventDto.getIp(), "/events");
            return List.of();
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