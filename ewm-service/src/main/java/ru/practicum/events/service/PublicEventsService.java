package ru.practicum.events.service;

import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.dto.LookEventDto;
import ru.practicum.events.dto.SearchEventsParams;
import ru.practicum.events.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface PublicEventsService {

    Event getEvent(Long id);

    int getEventsViews(long id, LocalDateTime eventDate);

    EventFullDto getEventInfo(LookEventDto lookEventDto);

    List<EventShortDto> getFilteredEvents(SearchEventsParams searchEventsParams, LookEventDto lookEventDto);

    Event getEventAnyStatusWithViews(Long id);

}
