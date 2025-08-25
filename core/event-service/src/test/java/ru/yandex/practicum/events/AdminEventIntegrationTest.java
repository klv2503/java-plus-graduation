package ru.yandex.practicum.events;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.events.client.EventCategoryFeign;
import ru.yandex.practicum.events.client.EventRequestFeign;
import ru.yandex.practicum.clients.UserServiceFeign;
import ru.yandex.practicum.config.DateConfig;
import ru.yandex.practicum.dto.category.CategoryDto;
import ru.yandex.practicum.errors.exceptions.ForbiddenActionException;
import ru.yandex.practicum.dto.events.EventFullDto;
import ru.yandex.practicum.dto.events.UpdateEventAdminRequest;
import ru.yandex.practicum.enums.EventStateAction;
import ru.yandex.practicum.enums.StateEvent;
import ru.yandex.practicum.events.service.AdminEventService;
import ru.yandex.practicum.events.validation.AdminEventValidator;
import ru.yandex.practicum.dto.user.UserDto;
import ru.yandex.practicum.events.model.Event;
import ru.yandex.practicum.events.model.Location;
import ru.yandex.practicum.events.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
public class AdminEventIntegrationTest {

    @MockBean
    private UserServiceFeign userServiceFeign;

    @MockBean
    private EventCategoryFeign eventCategoryFeign;

    @MockBean
    private EventRequestFeign eventRequestFeign;

    @Autowired
    private AdminEventService adminEventService;

    @Autowired
    private EventRepository eventRepository;

    private UserDto testUser;
    private CategoryDto testCategory;
    private CategoryDto testCategory2;
    private Event pendingEvent;

    @BeforeEach
    void setUp() {
        testUser = new UserDto();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("testuser@example.com");
        //testUser = userRepository.save(testUser);

        testCategory = CategoryDto.builder()
                .id(1L)
                .name("Test Category")
                .build();
        testCategory2 = CategoryDto.builder()
                .id(2L)
                .name("Test Category 2")
                .build();

        Location location = new Location();
        location.setLat(10.0f);
        location.setLon(20.0f);

        pendingEvent = new Event();
        pendingEvent.setTitle("Test Event");
        pendingEvent.setDescription("Event Description");
        pendingEvent.setAnnotation("Event Annotation");
        pendingEvent.setCategory(testCategory.getId());
        pendingEvent.setParticipantLimit(5);
        pendingEvent.setEventDate(LocalDateTime.now().plusDays(5));
        pendingEvent.setRequestModeration(false);
        pendingEvent.setPaid(false);
        pendingEvent.setLocation(location);
        pendingEvent.setState(StateEvent.PENDING);
        pendingEvent.setRating(0.0);
        pendingEvent.setInitiatorId(testUser.getId());
        pendingEvent.setCreatedOn(LocalDateTime.now());
        pendingEvent = eventRepository.save(this.pendingEvent);
    }

    @Test
    void getEvents_ShouldReturnEvents() {
        Map<Long, Long> participationCounts = new HashMap<>();
        participationCounts.put(pendingEvent.getId(), 3L);

        Mockito.when(eventRequestFeign.getParticipationCounts(Mockito.anyList()))
                .thenReturn(ResponseEntity.ok(participationCounts));

        List<EventFullDto> events = adminEventService.getEvents(
                Collections.singletonList(testUser.getId()),
                Collections.singletonList(StateEvent.PENDING.name()),
                Collections.singletonList(testCategory.getId()),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(10),
                0,
                10
        );

        assertThat(events).isNotEmpty();
        assertThat(events.get(0).getTitle()).isEqualTo("Test Event");
    }

    @Test
    void updateEvent_ShouldUpdateEventDetails() {
        Mockito.when(eventCategoryFeign.getInfoById(Mockito.anyLong()))
                .thenReturn(ResponseEntity.ok(testCategory2));
        LocalDateTime eventDate = LocalDateTime.now().plusDays(10);

        UpdateEventAdminRequest updateRequest = new UpdateEventAdminRequest();
        updateRequest.setTitle("Updated Event Title");
        updateRequest.setDescription("Updated Description");
        updateRequest.setAnnotation("Updated Annotation");
        updateRequest.setEventDate(eventDate);
        updateRequest.setCategory(testCategory2.getId().intValue());
        updateRequest.setPaid(false);
        updateRequest.setParticipantLimit(5);
        updateRequest.setStateAction(EventStateAction.PUBLISH_EVENT);

        EventFullDto updatedEvent = adminEventService.updateEvent(pendingEvent.getId(), updateRequest);

        assertThat(updatedEvent.getTitle()).isEqualTo("Updated Event Title");
        assertThat(updatedEvent.getDescription()).isEqualTo("Updated Description");
        assertThat(updatedEvent.getAnnotation()).isEqualTo("Updated Annotation");
        assertThat(updatedEvent.getEventDate()).isEqualTo(eventDate.format(DateConfig.FORMATTER));
        assertThat(updatedEvent.getCategory()).isEqualTo(testCategory2.getId());
        assertThat(updatedEvent.isPaid()).isEqualTo(false);
        assertThat(updatedEvent.getParticipantLimit()).isEqualTo(5);
        assertThat(updatedEvent.getState()).isEqualTo(StateEvent.PUBLISHED);
    }

    @Test
    void updateEvent_ShouldUpdateEventStatusCanceled() {
        UpdateEventAdminRequest updateRequest = new UpdateEventAdminRequest();
        updateRequest.setStateAction(EventStateAction.REJECT_EVENT);
        EventFullDto updatedEvent = adminEventService.updateEvent(pendingEvent.getId(), updateRequest);
        assertThat(updatedEvent.getState()).isEqualTo(StateEvent.CANCELED);
    }

    @Test
    void updateEvent_ShouldThrowException_WhenUpdateEventNonExistCategory() {
        Mockito.when(eventCategoryFeign.getInfoById(Mockito.anyLong()))
                .thenThrow(new EntityNotFoundException("Category with id not found"));
        UpdateEventAdminRequest updateRequest = new UpdateEventAdminRequest();
        updateRequest.setCategory(9999);

        assertThrows(EntityNotFoundException.class, () -> {
            adminEventService.updateEvent(pendingEvent.getId(), updateRequest);
        });
    }

    @Test
    void validateEventStatusUpdate_ShouldThrowException_WhenEventDateTooSoon() {
        UpdateEventAdminRequest updateRequest = new UpdateEventAdminRequest();
        updateRequest.setEventDate(pendingEvent.getCreatedOn().plusMinutes(30));

        Event event = eventRepository.getReferenceById(pendingEvent.getId());

        assertThrows(ForbiddenActionException.class, () ->
                AdminEventValidator.validateEventStatusUpdate(event, updateRequest));
    }

    @Test
    void validateEventStatusUpdate_ShouldPass_WhenEventDateIsValid() {
        UpdateEventAdminRequest updateRequest = new UpdateEventAdminRequest();
        updateRequest.setEventDate(pendingEvent.getCreatedOn().plusHours(2));

        Event event = eventRepository.getReferenceById(pendingEvent.getId());

        assertDoesNotThrow(() -> AdminEventValidator.validateEventStatusUpdate(event, updateRequest));
    }

    @Test
    void validateEventStatusUpdate_ShouldThrowException_WhenPublishingNonPendingEvent() {
        UpdateEventAdminRequest updateRequest = new UpdateEventAdminRequest();
        updateRequest.setStateAction(EventStateAction.PUBLISH_EVENT);

        Event event = eventRepository.getReferenceById(pendingEvent.getId());
        event.setState(StateEvent.CANCELED);

        assertThrows(ForbiddenActionException.class, () ->
                AdminEventValidator.validateEventStatusUpdate(event, updateRequest));
    }

    @Test
    void validateEventStatusUpdate_ShouldThrowException_WhenRejectingPublishedEvent() {
        UpdateEventAdminRequest updateRequest = new UpdateEventAdminRequest();
        updateRequest.setStateAction(EventStateAction.REJECT_EVENT);

        Event event = eventRepository.getReferenceById(pendingEvent.getId());
        event.setState(StateEvent.PUBLISHED);

        assertThrows(ForbiddenActionException.class, () ->
                AdminEventValidator.validateEventStatusUpdate(event, updateRequest));
    }

    @Test
    void validateEventStatusUpdate_ShouldPass_WhenValidStateTransition() {
        UpdateEventAdminRequest updateRequest = new UpdateEventAdminRequest();
        updateRequest.setStateAction(EventStateAction.PUBLISH_EVENT);

        Event event = eventRepository.getReferenceById(pendingEvent.getId());
        event.setState(StateEvent.PENDING);

        assertDoesNotThrow(() -> AdminEventValidator.validateEventStatusUpdate(event, updateRequest));
    }

    @Test
    void validateEventStatusUpdate_ShouldPass_WhenStateActionIsNull() {
        UpdateEventAdminRequest updateRequest = new UpdateEventAdminRequest();
        updateRequest.setStateAction(null);

        Event event = eventRepository.getReferenceById(pendingEvent.getId());

        assertDoesNotThrow(() -> AdminEventValidator.validateEventStatusUpdate(event, updateRequest));
    }


    @Test
    void updateEvent_ShouldThrowEntityNotFoundException_WhenEventNotExists() {
        UpdateEventAdminRequest updateRequest = new UpdateEventAdminRequest();
        updateRequest.setTitle("Nonexistent Event");

        assertThrows(EntityNotFoundException.class, () ->
                adminEventService.updateEvent(9999L, updateRequest));
    }
}

