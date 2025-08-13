package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.events.EventFullDto;
import ru.yandex.practicum.dto.events.EventShortDto;
import ru.yandex.practicum.dto.events.NewEventDto;
import ru.yandex.practicum.dto.events.UpdateEventUserRequest;
import ru.yandex.practicum.dto.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.yandex.practicum.dto.events.GetUserEventsDto;
import ru.yandex.practicum.dto.request.ParticipationRequestDto;
import ru.yandex.practicum.service.PrivateUserEventService;

import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/events")
@Validated
@Slf4j
@RequiredArgsConstructor
public class PrivateUserEventController {
    private final PrivateUserEventService privateUserEventService;

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getUserEvents(@PathVariable("userId") Long userId,
                                                             @RequestParam(required = false, defaultValue = "0") int from,
                                                             @RequestParam(required = false, defaultValue = "10") int size) {
        log.info("\nRequest getting user {} events", userId);
        GetUserEventsDto dto = new GetUserEventsDto(userId, from, size);
        return ResponseEntity.status(HttpStatus.OK).body(privateUserEventService.getUsersEvents(dto));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> getUserEventById(@PathVariable("userId") Long userId,
                                                         @PathVariable("eventId") Long eventId) {
        log.info("\nRequest getting user {} event {}", userId, eventId);
        return ResponseEntity.status(HttpStatus.OK).body(privateUserEventService.getUserEventById(userId, eventId));
    }

    @PostMapping
    public ResponseEntity<EventFullDto> addNewEvent(@PathVariable("userId") Long userId,
                                                    @Valid @RequestBody NewEventDto eventDto) {
        log.info("\nRequest for adding new event {}", eventDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(privateUserEventService.addNewEvent(userId, eventDto));
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateUserEvent(@PathVariable("userId") Long userId,
                                                        @PathVariable("eventId") Long eventId,
                                                        @Valid @RequestBody UpdateEventUserRequest updateDto) {
        log.info("\nRequest for updating existing event {}", updateDto);
        return ResponseEntity.status(HttpStatus.OK).body(privateUserEventService.updateUserEvent(userId, eventId, updateDto));
    }

    @GetMapping("/{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getUserEventRequests(@PathVariable("userId") Long userId,
                                                                              @PathVariable("eventId") Long eventId) {
        log.info("\nRequest getting user {} event {} requests", userId, eventId);
        return ResponseEntity.status(HttpStatus.OK).body(privateUserEventService.getUserEventRequests(userId, eventId));
    }

    @PatchMapping("/{eventId}/requests")
    public ResponseEntity<EventRequestStatusUpdateResult> updateUserEventRequestStatus(@PathVariable("userId") Long userId,
                                                                                       @PathVariable("eventId") Long eventId,
                                                                                       @RequestBody EventRequestStatusUpdateRequest request) {
        log.info("RequestIds: {}, Status: {}", request.getRequestIds(), request.getStatus());
        return ResponseEntity.status(HttpStatus.OK).body(privateUserEventService.updateUserEventRequest(userId, eventId, request));
    }

}
