package ru.yandex.practicum.events.private_events;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.category.service.CategoryServiceImpl;
import ru.yandex.practicum.events.dto.EventFullDto;
import ru.yandex.practicum.events.dto.EventShortDto;
import ru.yandex.practicum.events.dto.NewEventDto;
import ru.yandex.practicum.events.dto.UpdateEventUserRequest;
import ru.yandex.practicum.events.model.Event;
import ru.yandex.practicum.events.model.Location;
import ru.yandex.practicum.events.model.StateEvent;
import ru.yandex.practicum.events.repository.EventRepository;
import ru.yandex.practicum.events.service.PublicEventsServiceImpl;
import ru.yandex.practicum.users.dto.GetUserEventsDto;
import ru.yandex.practicum.users.service.PrivateUserEventService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional(readOnly = true)
@Rollback(value = false)
@TestPropertySource(locations = "classpath:application-test.properties")
public class PrivateUserEventsIntegrationTest {
    @Autowired
    private PrivateUserEventService privateUserEventService;
    @Autowired
    private CategoryServiceImpl categoryService;
    @Autowired
    private EventRepository eventRepository;

    @MockBean
    private PublicEventsServiceImpl publicEventsService;

    private final Location location = new Location(1L, 37, 56);
    private final NewEventDto eventDto = new NewEventDto(6L, "annotation", 1L, "descr", "2024-12-31 15:10:05", location, true, 10, false, "Title");

    @Test
    void savingNewEvent() {
        EventFullDto fullEventDto = privateUserEventService.addNewEvent(1L, eventDto);

        assertAll(
                () -> assertEquals(eventDto.getTitle(), fullEventDto.getTitle()),
                () -> assertEquals(eventDto.getAnnotation(), fullEventDto.getAnnotation()),
                () -> assertEquals(eventDto.getCategory(), fullEventDto.getCategory().getId()),
                () -> assertEquals(eventDto.getDescription(), fullEventDto.getDescription()),
                () -> assertEquals(eventDto.getLocation(), fullEventDto.getLocation()),
                () -> assertEquals(eventDto.isPaid(), fullEventDto.isPaid()),
                () -> assertEquals(eventDto.getParticipantLimit(), fullEventDto.getParticipantLimit()),
                () -> assertEquals(eventDto.getRequestModeration(), fullEventDto.isRequestModeration()),
                () -> assertEquals(eventDto.getTitle(), fullEventDto.getTitle())
        );
    }

    @Test
    void getUserEvents() {
        EventFullDto fullEventDto = privateUserEventService.addNewEvent(1L, eventDto);
        eventDto.setTitle("new descr");
        eventDto.setId(6L);
        privateUserEventService.addNewEvent(1L, eventDto);

        GetUserEventsDto dto = new GetUserEventsDto(1L, 0, 10);

        List<EventShortDto> dtoList = privateUserEventService.getUsersEvents(dto);

        assertAll(
                () -> assertEquals(dtoList.size(), 2),
                () -> assertEquals(dtoList.getFirst().getTitle(), "Title"),
                () -> assertEquals(dtoList.getFirst().getAnnotation(), eventDto.getAnnotation()),
                () -> assertEquals(dtoList.getFirst().isPaid(), eventDto.isPaid())
        );
    }

    @Test
    void getUserEventById() {
        Event event =  eventRepository.findById(1L).orElseThrow();
        event.setTitle("New title");
        event.setCreatedOn(LocalDateTime.now());

        eventRepository.save(event);

        EventFullDto fullEventDto = privateUserEventService.getUserEventById(event.getInitiator().getId(), event.getId());

        assertAll(
                () -> assertEquals(event.getTitle(), "New title"),
                () -> assertEquals(event.getAnnotation(), fullEventDto.getAnnotation()),
                () -> assertEquals(event.isPaid(), fullEventDto.isPaid())
        );
    }

    @Test
    void updatingEvent() {
        Event event =  eventRepository.findById(1L).orElseThrow();
        event.setState(StateEvent.CANCELED);
        event.setCreatedOn(LocalDateTime.now());
        UpdateEventUserRequest updateRequest = new UpdateEventUserRequest(1L, "annotationannotationannotation", 1, "descrdescrdescrdescrdescrdescrdescrdescrdescrdescrdescrdescrdescrdescrdescr",
                "2025-12-31 15:10:05", location,
                true, 10, true, "CANCEL_REVIEW", "Title");

        EventFullDto updatedEvent = privateUserEventService.updateUserEvent(event.getInitiator().getId(), event.getId(), updateRequest);

        assertAll(
                () -> assertEquals(updateRequest.getTitle(), updatedEvent.getTitle()),
                () -> assertEquals(updateRequest.getAnnotation(), updatedEvent.getAnnotation()),
                () -> assertEquals(updateRequest.getCategory(), updatedEvent.getCategory().getId()),
                () -> assertEquals(updateRequest.getDescription(), updatedEvent.getDescription()),
                () -> assertEquals(updateRequest.getLocation(), updatedEvent.getLocation()),
                () -> assertEquals(updatedEvent.isPaid(), true),
                () -> assertEquals(event.getParticipantLimit(), updatedEvent.getParticipantLimit()),
                () -> assertEquals(updateRequest.isRequestModeration(), updatedEvent.isRequestModeration()),
                () -> assertEquals(updateRequest.getTitle(), updatedEvent.getTitle())
        );
    }
}
