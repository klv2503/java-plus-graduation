package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.config.DateConfig;
import ru.practicum.dto.CreateEndpointHitDto;
import ru.practicum.dto.ManyEndPointDto;
import ru.practicum.dto.TakeHitsDto;
import ru.practicum.dto.ReadEndpointHitDto;
import ru.practicum.service.EndpointHitService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("")
@Slf4j
public class StatsController {

    private final EndpointHitService endpointHitService;

    @Autowired
    public StatsController(EndpointHitService endpointHitService) {
        this.endpointHitService = endpointHitService;
    }

    @PostMapping("/hit")
    public ResponseEntity<Void> saveHit(@Valid @RequestBody CreateEndpointHitDto dto) {
        endpointHitService.saveHit(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/stats")
    public ResponseEntity<Collection<ReadEndpointHitDto>> getHits(@RequestParam
                                                                  @DateTimeFormat(pattern = DateConfig.FORMAT)
                                                                  LocalDateTime start,
                                                                  @RequestParam
                                                                  @DateTimeFormat(pattern = DateConfig.FORMAT)
                                                                  LocalDateTime end,
                                                                  @RequestParam(required = false)
                                                                  Optional<List<String>> uris,
                                                                  @RequestParam(required = false, defaultValue = "false")
                                                                  boolean unique) {
        TakeHitsDto takeHitsDto = TakeHitsDto.builder()
                .start(start)
                .end(end)
                .uris(uris.orElse(List.of()))
                .unique(unique)
                .build();
        log.info("\nStatsController.getHits accepted {}", takeHitsDto);
        return ResponseEntity.status(HttpStatus.OK).body(endpointHitService.getHits(takeHitsDto));
    }

    @PostMapping("/hit/group")
    public ResponseEntity<Void> saveHitGroup(@RequestBody ManyEndPointDto many) {
        log.info("\nStatsController.saveHitsGroup many {}", many);

        endpointHitService.saveHitsGroup(many);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}