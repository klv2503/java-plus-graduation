package ru.yandex.practicum.events.service;

import ru.yandex.practicum.events.dto.LookEventDto;
import ru.yandex.practicum.events.dto.SearchEventsParams;
import ru.yandex.practicum.dto.events.EventFullDto;
import ru.yandex.practicum.dto.events.EventShortDto;
import ru.yandex.practicum.events.model.Event;

import java.util.List;

public interface PublicEventsService {

    Event getEvent(Long id);

    double getEventsRating(long id);

    EventFullDto getEventInfo(LookEventDto lookEventDto);

    List<EventShortDto> getFilteredEvents(SearchEventsParams searchEventsParams, LookEventDto lookEventDto);

    EventFullDto getEventAnyStatusWithRating(Long id);

    List<Event> getEventsByListIds(List<Long> ids);

    List<EventShortDto> getRecommendations4User(Long userId, int quant);

    void putUsersLike(Long eventId, Long userId);
}
