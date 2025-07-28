package ru.practicum.users;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.MainService;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.service.CategoryService;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.NewEventDto;
import ru.practicum.events.dto.UpdateEventAdminRequest;
import ru.practicum.events.model.EventStateAction;
import ru.practicum.events.model.Location;
import ru.practicum.events.service.AdminEventService;
import ru.practicum.users.dto.ParticipationRequestDto;
import ru.practicum.users.errors.EventOwnerParticipationException;
import ru.practicum.users.errors.EventParticipationLimitException;
import ru.practicum.users.errors.NotPublishedEventParticipationException;
import ru.practicum.users.errors.RepeatParticipationRequestException;
import ru.practicum.users.model.ParticipationRequestStatus;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;
import ru.practicum.users.service.ParticipationRequestService;
import ru.practicum.users.service.PrivateUserEventService;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = "classpath:application-test.properties", classes = MainService.class)
@ExtendWith(SpringExtension.class)
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class RequestsForParticipationIntegrationTest {
    @Autowired
    private ParticipationRequestService participationRequestService;

    @Autowired
    private PrivateUserEventService eventService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminEventService adminEventService;

    private User eventOwner;
    private User eventParticipant;
    private User eventSecondParticipant;
    private EventFullDto pendingEvent;

    @BeforeEach
    void setUp() {
        eventOwner = new User();
        eventOwner.setName("Test User");
        eventOwner.setEmail("eventOwner@example.com");
        eventOwner = userRepository.save(eventOwner);

        eventParticipant = new User();
        eventParticipant.setName("Test User");
        eventParticipant.setEmail("eventParticipant@example.com");
        eventParticipant = userRepository.save(eventParticipant);

        eventSecondParticipant = new User();
        eventSecondParticipant.setName("Test User");
        eventSecondParticipant.setEmail("eventSecondParticipant@example.com");
        eventSecondParticipant = userRepository.save(eventSecondParticipant);

        NewCategoryDto newCategory = new NewCategoryDto();
        newCategory.setName("Test Category");
        CategoryDto category = categoryService.addCategory(newCategory);

        Location location = new Location();
        location.setLat(0.12f);
        location.setLon(0.11f);

        NewEventDto newEventDto = new NewEventDto();
        newEventDto.setTitle("Test Event");
        newEventDto.setDescription("Description");
        newEventDto.setAnnotation("some annotation");
        newEventDto.setCategory(Math.toIntExact(category.getId()));
        newEventDto.setParticipantLimit(1);
        newEventDto.setEventDate("9999-02-02 12:12:12");
        newEventDto.setRequestModeration(false);
        newEventDto.setPaid(false);
        newEventDto.setLocation(location);
        pendingEvent = eventService.addNewEvent(eventOwner.getId(), newEventDto);
    }

    @Test
    void getUserRequests_ShouldReturnEmptyListInitially() {
        List<ParticipationRequestDto> requests = participationRequestService.getUserRequests(eventOwner.getId());
        assertThat(requests).isEmpty();
    }

    @Test
    @Transactional
    void addParticipationRequest_ShouldCreateNewRequest() {
        UpdateEventAdminRequest updateEvent = new UpdateEventAdminRequest();
        updateEvent.setStateAction(EventStateAction.PUBLISH_EVENT);
        EventFullDto eventFullDto = adminEventService.updateEvent(pendingEvent.getId(), updateEvent);

        ParticipationRequestDto requestDto = participationRequestService
                .addParticipationRequest(eventParticipant.getId(), eventFullDto.getId());

        assertThat(requestDto).isNotNull();
        assertThat(requestDto.getRequester()).isEqualTo(eventParticipant.getId());
        assertThat(requestDto.getEvent()).isEqualTo(pendingEvent.getId());
        assertThat(requestDto.getStatus()).isEqualTo(ParticipationRequestStatus.CONFIRMED);
    }

    @Test
    @Transactional
    void addParticipationRequest_ShouldThrowError() {
        assertThrows(EventOwnerParticipationException.class, () -> {
            participationRequestService.addParticipationRequest(eventOwner.getId(), pendingEvent.getId());
        });

        assertThrows(NotPublishedEventParticipationException.class, () -> {
            participationRequestService.addParticipationRequest(eventParticipant.getId(), pendingEvent.getId());
        });

        UpdateEventAdminRequest updateEvent = new UpdateEventAdminRequest();
        updateEvent.setStateAction(EventStateAction.PUBLISH_EVENT);
        EventFullDto eventFullDto = adminEventService.updateEvent(pendingEvent.getId(), updateEvent);

        assertThrows(EventParticipationLimitException.class, () -> {
            participationRequestService.addParticipationRequest(eventParticipant.getId(), eventFullDto.getId());
            participationRequestService.addParticipationRequest(eventSecondParticipant.getId(), eventFullDto.getId());
        });

        assertThrows(RepeatParticipationRequestException.class, () -> {
            participationRequestService.addParticipationRequest(eventParticipant.getId(), eventFullDto.getId());
        });
    }

    @Test
    @Transactional
    void cancelRequest_ShouldChangeRequestStatusToCanceled() {
        UpdateEventAdminRequest updateEvent = new UpdateEventAdminRequest();
        updateEvent.setStateAction(EventStateAction.PUBLISH_EVENT);
        EventFullDto eventFullDto = adminEventService.updateEvent(pendingEvent.getId(), updateEvent);

        ParticipationRequestDto requestDto = participationRequestService
                .addParticipationRequest(eventParticipant.getId(), eventFullDto.getId());
        ParticipationRequestDto canceledRequest = participationRequestService
                .cancelRequest(eventParticipant.getId(), requestDto.getId());

        assertThat(canceledRequest.getStatus()).isEqualTo(ParticipationRequestStatus.CANCELED);
    }
}
