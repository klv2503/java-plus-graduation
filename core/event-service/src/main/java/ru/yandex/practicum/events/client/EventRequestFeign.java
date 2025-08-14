package ru.yandex.practicum.events.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.request.ChangeRequestStatus;
import ru.yandex.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.yandex.practicum.dto.request.ParticipationRequestDto;
import ru.yandex.practicum.enums.ParticipationRequestStatus;

import java.util.List;
import java.util.Map;

@FeignClient(name = "request-service", path = "/requests")
public interface EventRequestFeign {

    @GetMapping
    ResponseEntity<Map<Long, Long>> getParticipationCounts(@RequestParam List<Long> ids);

    @GetMapping("/{eventId}/count")
    ResponseEntity<Integer> getParticipationWithStatus(@PathVariable Long eventId,
                                                       @RequestParam ParticipationRequestStatus status);

    @GetMapping("/{eventId}/list")
    ResponseEntity<List<ParticipationRequestDto>> getRequestList(@PathVariable Long eventId);

    @PostMapping
    ResponseEntity<EventRequestStatusUpdateResult> changeRequestStatuses(@RequestBody ChangeRequestStatus changeRequestStatus);

}
