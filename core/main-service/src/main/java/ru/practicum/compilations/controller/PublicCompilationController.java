package ru.practicum.compilations.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.Filter;
import ru.practicum.compilations.service.CompilationService;

import java.util.List;

@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
@Slf4j
public class PublicCompilationController {

    private final CompilationService compilationService;

    @GetMapping("/{compId}")
    public ResponseEntity<CompilationDto> getById(@PathVariable
                                                  @Min(value = 1, message = "ID must be positive")
                                                  Long compId
    ) {
        log.info("Accepted request for get compilation with id {}", compId);
        return ResponseEntity.status(HttpStatus.OK).body(compilationService.getById(compId));
    }

    @GetMapping
    public ResponseEntity<List<CompilationDto>> get(@RequestParam(required = false, defaultValue = "false") Boolean pinned,
                                                    @RequestParam(required = false, defaultValue = "0") Integer from,
                                                    @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("Accepted request for get compilations");
        Filter params = new Filter(pinned, from, size);
        return ResponseEntity.status(HttpStatus.OK).body(compilationService.get(params));
    }
}
