package ru.yandex.practicum.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.request.ChangeRequestStatus;
import ru.yandex.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.yandex.practicum.dto.request.ParticipationRequestDto;
import ru.yandex.practicum.enums.ParticipationRequestStatus;
import ru.yandex.practicum.service.ParticipationRequestService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class AdminRequestController {

    private final ParticipationRequestService service;

    @GetMapping
    public ResponseEntity<Map<Long, Long>> getParticipationCounts(@RequestParam List<Long> ids) {
        return ResponseEntity.ok(service.getParticipationCounts(ids));
    }

    @GetMapping("/{eventId}/count")
    public ResponseEntity<Integer> getParticipationWithStatus(@PathVariable @NotNull @Positive Long eventId,
                                                              @RequestParam ParticipationRequestStatus status) {
        return ResponseEntity.ok(service.getParticipationWithStatus(eventId, status));
    }

    @GetMapping("/{eventId}/list")
    public ResponseEntity<List<ParticipationRequestDto>> getRequestList(@PathVariable @NotNull @Positive Long eventId) {
        return ResponseEntity.ok(service.getRequestList(eventId));
    }

    @PostMapping
    public ResponseEntity<EventRequestStatusUpdateResult> changeRequestStatuses(
            @RequestBody ChangeRequestStatus changeRequestStatus) {
        return ResponseEntity.ok(service.changeRequestStatuses(changeRequestStatus));
    }
}
