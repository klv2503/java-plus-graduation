package ru.practicum.compilations.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.NewCompilationDto;
import ru.practicum.compilations.dto.UpdateCompilationRequest;
import ru.practicum.compilations.service.CompilationService;

@RestController
@RequestMapping(path = "/admin/compilations")
@Validated
@Slf4j
@RequiredArgsConstructor
public class AdminCompilationController {

    private final CompilationService compilationService;

    @PostMapping
    public ResponseEntity<CompilationDto> post(@RequestBody @Valid NewCompilationDto newCompilationDto) {
        log.info("Request for adding new compilation {}", newCompilationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(compilationService.add(newCompilationDto));
    }

    @PatchMapping("/{compId}")
    public ResponseEntity<CompilationDto> update(@PathVariable @Min(value = 1, message = "ID must be positive")
                                                 Long compId,
                                                 @RequestBody @Valid UpdateCompilationRequest updateCompilationRequest) {
        log.info("Request for update of compilation with id {}", compId);
        return ResponseEntity.status(HttpStatus.OK).body(compilationService.update(compId, updateCompilationRequest));
    }

    @DeleteMapping("/{compId}")
    public ResponseEntity<Void> delete(@PathVariable @Min(value = 1, message = "ID must be positive")
                                       Long compId) {
        log.info("Request for delete compilation with id {}", compId);
        compilationService.delete(compId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
