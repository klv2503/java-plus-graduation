package ru.practicum.compilations;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.MainService;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.Filter;
import ru.practicum.compilations.dto.NewCompilationDto;
import ru.practicum.compilations.dto.UpdateCompilationRequest;
import ru.practicum.compilations.repository.CompilationRepository;
import ru.practicum.compilations.service.CompilationService;
import ru.practicum.events.model.Event;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.events.service.PublicEventsServiceImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = "classpath:application-test.properties",
        classes = MainService.class)
@ExtendWith(SpringExtension.class)
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class CompilationIntegrationTest {

    @Autowired
    private CompilationService compilationService;
    @Autowired
    private CompilationRepository compilationRepository;
    @Autowired
    private EventRepository eventRepository;
    @MockBean
    private PublicEventsServiceImpl publicEventsService;

    @Test
    @Transactional
    public void addCompilationTest() {
        List<Event> events = eventRepository.findAllById(List.of(3L, 4L));
        events.forEach(event -> {
            event.setConfirmedRequests(10);
            event.setViews(20);
        });

        when(publicEventsService.getEventsByListIds(List.of(3L, 4L))).thenReturn(events);

        NewCompilationDto newCompilationDto = new NewCompilationDto(Set.of(3L, 4L), false, "Title");
        CompilationDto compilation = compilationService.add(newCompilationDto);

        assertAll(
                () -> assertEquals(newCompilationDto.getPinned(), compilation.getPinned()),
                () -> assertEquals(newCompilationDto.getTitle(), compilation.getTitle()),
                () -> assertNotNull(compilation.getId())
        );
    }

    @Test
    @Transactional
    public void updatePinnedCompilationTest() {
        Event event = eventRepository.findById(1L).get();
        event.setConfirmedRequests(15);
        event.setViews(15);

        when(publicEventsService.getEventsByListIds(List.of(1L))).thenReturn(List.of(event));

        UpdateCompilationRequest updateCompilationRequest = new UpdateCompilationRequest(
                null,
                true,
                null);
        CompilationDto compilation = compilationService.update(1L, updateCompilationRequest);

        assertAll(
                () -> assertNotNull(compilation.getEvents()),
                () -> assertEquals(updateCompilationRequest.getPinned(), compilation.getPinned()),
                () -> assertEquals(1L, compilation.getId())
        );
    }

    @Test
    @Transactional
    public void updateTitleCompilationTest() {
        UpdateCompilationRequest updateCompilationRequest = new UpdateCompilationRequest(
                null,
                null,
                "New title"
        );

        CompilationDto compilation = compilationService.update(1L, updateCompilationRequest);

        assertAll(
                () -> assertNotNull(compilation.getEvents()),
                () -> assertNotNull(compilation.getPinned()),
                () -> assertEquals(updateCompilationRequest.getTitle(), compilation.getTitle()),
                () -> assertEquals(1L, compilation.getId())
        );
    }

    @Test
    @Transactional
    public void updateEventsCompilationTest() {
        Event event = eventRepository.findById(2L).get();
        event.setConfirmedRequests(15);
        event.setViews(15);

        when(publicEventsService.getEventsByListIds(List.of(2L))).thenReturn(List.of(event));

        Set<Long> events = new HashSet<>();
        events.add(2L);

        UpdateCompilationRequest updateCompilationRequest = new UpdateCompilationRequest(
                events,
                null,
                null
        );

        CompilationDto compilation = compilationService.update(2L, updateCompilationRequest);
        List<Long> eventsId = compilation.getEvents().stream()
                .map(eventShortDto -> eventShortDto.getId()).toList();

        assertAll(
                () -> assertEquals(1, compilation.getEvents().size()),
                () -> assertTrue(eventsId.contains(2L))
        );
    }

    @Test
    @Transactional
    public void deleteCompilationTest() {
        compilationService.delete(3L);

        assertFalse(compilationRepository.existsById(3L));
    }

    @Test
    public void deleteCompilationWithWrongIdTest() {
        assertThrows(EntityNotFoundException.class, () -> compilationService.delete(1000L));
    }

    @Test
    public void getCompilationTest() {
        Event event = eventRepository.findById(1L).get();
        event.setConfirmedRequests(15);
        event.setViews(15);

        when(publicEventsService.getEventsByListIds(List.of(1L))).thenReturn(List.of(event));

        CompilationDto compilation = compilationService.getById(1L);

        assertAll(
                () -> assertEquals(1, compilation.getId()),
                () -> assertEquals(1, compilation.getEvents().size())
        );
    }

    @Test
    public void getCompilationPinnedTrueTest() {
        Filter params = new Filter(true, 0, 10);
        List<CompilationDto> compilationList = compilationService.get(params);
        List<Boolean> pinned = compilationList.stream().map(compilation -> compilation.getPinned()).toList();

        assertAll(
                () -> assertFalse(compilationList.isEmpty()),
                () -> assertFalse(pinned.contains(false))
        );
    }

    @Test
    public void getCompilationSizeTest() {
        Filter params = new Filter(false, 0, 2);
        List<CompilationDto> compilationList = compilationService.get(params);

        assertEquals(2, compilationList.size());
    }
}
