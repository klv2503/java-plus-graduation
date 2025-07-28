package ru.practicum.events;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.MainService;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.config.DateConfig;
import ru.practicum.errors.ForbiddenActionException;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.UpdateEventAdminRequest;
import ru.practicum.events.model.Event;
import ru.practicum.events.model.EventStateAction;
import ru.practicum.events.model.Location;
import ru.practicum.events.model.StateEvent;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.events.service.AdminEventService;
import ru.practicum.events.validation.AdminEventValidator;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = MainService.class)
@ExtendWith(SpringExtension.class)
@Transactional
public class AdminEventIntegrationTest {

    @Autowired
    private AdminEventService adminEventService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Category testCategory;
    private Category testCategory2;
    private Event pendingEvent;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("testuser@example.com");
        testUser = userRepository.save(testUser);

        testCategory = new Category();
        testCategory.setName("Test Category");
        categoryRepository.save(testCategory);

        testCategory2 = new Category();
        testCategory2.setName("Test Category 2");
        categoryRepository.save(testCategory2);

        Location location = new Location();
        location.setLat(10.0f);
        location.setLon(20.0f);

        pendingEvent = new Event();
        pendingEvent.setTitle("Test Event");
        pendingEvent.setDescription("Event Description");
        pendingEvent.setAnnotation("Event Annotation");
        pendingEvent.setCategory(testCategory);
        pendingEvent.setParticipantLimit(5);
        pendingEvent.setEventDate(LocalDateTime.now().plusDays(5));
        pendingEvent.setRequestModeration(false);
        pendingEvent.setPaid(false);
        pendingEvent.setLocation(location);
        pendingEvent.setState(StateEvent.PENDING);
        pendingEvent.setViews(0);
        pendingEvent.setInitiator(testUser);
        pendingEvent.setCreatedOn(LocalDateTime.now());
        pendingEvent = eventRepository.save(this.pendingEvent);
    }

    @Test
    void getEvents_ShouldReturnEvents() {
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
        assertThat(updatedEvent.getCategory().getId()).isEqualTo(testCategory2.getId());
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

