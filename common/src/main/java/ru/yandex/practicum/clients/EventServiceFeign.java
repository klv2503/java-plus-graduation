package ru.yandex.practicum.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.dto.events.EventFullDto;
import ru.yandex.practicum.dto.events.EventShortDto;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "event-service")
public interface EventServiceFeign {

    @GetMapping("/admin/events/{eventId}")
    ResponseEntity<EventFullDto> getEventInfo(@PathVariable Long eventId);

    @GetMapping("/events/list")
    ResponseEntity<List<EventShortDto>> getEventsByListIds(@RequestParam List<Long> ids);

    @GetMapping("/{id}/full")
    ResponseEntity<EventFullDto> getEventAnyStatusWithViews(@PathVariable Long id);

    @GetMapping("/admin/events")
    ResponseEntity<List<EventFullDto>> getEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) LocalDateTime rangeStart,
            @RequestParam(required = false) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size);
}
