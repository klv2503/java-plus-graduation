package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.LookEventDto;
import ru.yandex.practicum.dto.SearchEventsParams;
import ru.yandex.practicum.dto.events.EventFullDto;
import ru.yandex.practicum.dto.events.EventShortDto;
import ru.yandex.practicum.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface PublicEventsService {

    Event getEvent(Long id);

    int getEventsViews(long id, LocalDateTime eventDate);

    EventFullDto getEventInfo(LookEventDto lookEventDto);

    List<EventShortDto> getFilteredEvents(SearchEventsParams searchEventsParams, LookEventDto lookEventDto);

    EventFullDto getEventAnyStatusWithViews(Long id);

    List<Event> getEventsByListIds(List<Long> ids);

}
