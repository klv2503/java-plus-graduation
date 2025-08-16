package ru.yandex.practicum.compilations.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.clients.EventServiceFeign;
import ru.yandex.practicum.compilations.dto.CompilationDto;
import ru.yandex.practicum.compilations.dto.Filter;
import ru.yandex.practicum.compilations.dto.NewCompilationDto;
import ru.yandex.practicum.compilations.dto.UpdateCompilationRequest;
import ru.yandex.practicum.compilations.model.Compilation;
import ru.yandex.practicum.compilations.repository.CompilationRepository;
import ru.yandex.practicum.dto.events.EventShortDto;
import ru.yandex.practicum.events.mapper.EventMapper;
import ru.yandex.practicum.events.service.PublicEventsService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.yandex.practicum.compilations.mapper.CompilationToCompilationDto.mapCompilationToCompilationDto;
import static ru.yandex.practicum.compilations.mapper.CompilationToCompilationDto.mapToListCompilationDto;
import static ru.yandex.practicum.compilations.mapper.NewCompilationDtoToCompilationMapper.mapNewCompilationDtoToCompilation;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final PublicEventsService publicEventsService;
    private final EventServiceFeign eventFeign;

    @Override
    public CompilationDto getById(Long compId) {
        log.info("Get compilation with id {}", compId);
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new EntityNotFoundException("Compilation with id=" + compId +
                        " was not found"));
        return mapCompilationToCompilationDto(compilation, getEventsListForDto(compilation));
    }

    @Override
    @Transactional
    public CompilationDto add(NewCompilationDto newCompilationDto) {
        log.info("Add compilation {}", newCompilationDto);
        Compilation newCompilation = compilationRepository
                .save(mapNewCompilationDtoToCompilation(newCompilationDto));
        return mapCompilationToCompilationDto(newCompilation, getEventsListForDto(newCompilation));
    }

    @Override
    @Transactional
    public CompilationDto update(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        log.info("Update compilation with id {}", compId);
        Compilation compilation = getCompilation(compId);

        if (updateCompilationRequest.getEvents() != null) {
            compilation.setEvents(updateCompilationRequest.getEvents());
        }

        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }

        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }
        Compilation updateCompilation = compilationRepository.save(compilation);
        return mapCompilationToCompilationDto(updateCompilation, getEventsListForDto(updateCompilation));
    }

    @Override
    @Transactional
    public void delete(Long compId) {
        log.info("Delete compilation with id {}", compId);
        if (!compilationRepository.existsById(compId)) {
            throw new EntityNotFoundException("Compilation with id=" + compId +
                    " was not found");
        }

        compilationRepository.deleteById(compId);
    }

    @Override
    public List<CompilationDto> get(Filter params) {
        log.info("Get compilations with filter {}", params);
        int page = params.getFrom() / params.getSize();
        Pageable pageable = PageRequest.of(page, params.getSize());
        Page<Compilation> response = params.getPinned() ? compilationRepository.findAllByPinnedTrue(pageable) :
                compilationRepository.findAll(pageable);
        List<Compilation> compilations = response.getContent().stream().toList();
        return mapToListCompilationDto(compilations, getEventShortDtoForListDto(compilations));
    }


    //вспомогательные методы
    private Compilation getCompilation(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new EntityNotFoundException("Compilation with id=" + compId +
                        " was not found"));
    }

    private HashMap<Long, EventShortDto> getEventShortDtoForListDto(List<Compilation> compilations) {
        List<Long> eventsId = compilations.stream()
                .map(compilation -> compilation.getEvents().stream().toList())
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());

        return new HashMap<>(
                publicEventsService.getEventsByListIds(eventsId).stream()
                        .map(EventMapper::toEventShortDto)
                        .collect(Collectors.toMap(EventShortDto::getId, Function.identity()))
        );
    }

    private List<EventShortDto> getEventsListForDto(Compilation compilation) {
        if (compilation.getEvents() == null || compilation.getEvents().isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        List<EventShortDto> events = EventMapper.toListEventShortDto(publicEventsService.getEventsByListIds(compilation.getEvents().stream().toList()));
        log.info("EventsShortDto: {}", events);
        return events;
    }

}
