package ru.practicum.users.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.users.dto.ParticipationRequestDto;
import ru.practicum.users.service.ParticipationRequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@Slf4j
@RequiredArgsConstructor
public class PrivateRequestsForParticipation {

    private final ParticipationRequestService participationRequestService;

    @GetMapping
    public ResponseEntity<List<ParticipationRequestDto>> getUserRequests(@PathVariable Long userId) {
        log.info("Request to get participation requests for user {}", userId);
        List<ParticipationRequestDto> participationRequests = participationRequestService.getUserRequests(userId);
        return ResponseEntity.ok(participationRequests);
    }

    @PostMapping
    public ResponseEntity<ParticipationRequestDto> addParticipationRequest(
            @PathVariable Long userId,
            @RequestParam Long eventId) {
        log.info("Request to add participation request for user {} for event {}", userId, eventId);
        ParticipationRequestDto participationRequest = participationRequestService.addParticipationRequest(userId, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(participationRequest);
    }

    @PatchMapping("/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> cancelRequest(
            @PathVariable Long userId,
            @PathVariable Long requestId) {
        log.info("Request to cancel participation request for user {} and request {}", userId, requestId);
        ParticipationRequestDto cancelledRequest = participationRequestService.cancelRequest(userId, requestId);
        return ResponseEntity.ok(cancelledRequest);
    }
}