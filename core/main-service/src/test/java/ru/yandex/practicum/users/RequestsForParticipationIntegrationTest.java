package ru.yandex.practicum.users;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.client.UserServiceFeign;
import ru.yandex.practicum.dto.category.CategoryDto;
import ru.yandex.practicum.category.dto.NewCategoryDto;
import ru.yandex.practicum.category.service.CategoryService;
import ru.yandex.practicum.dto.events.EventFullDto;
import ru.yandex.practicum.dto.events.NewEventDto;
import ru.yandex.practicum.dto.user.UserDto;
import ru.yandex.practicum.events.dto.UpdateEventAdminRequest;
import ru.yandex.practicum.events.mapper.LocationMapper;
import ru.yandex.practicum.events.model.EventStateAction;
import ru.yandex.practicum.events.model.Location;
import ru.yandex.practicum.events.service.AdminEventService;
import ru.yandex.practicum.users.dto.ParticipationRequestDto;
import ru.yandex.practicum.users.errors.EventOwnerParticipationException;
import ru.yandex.practicum.users.errors.EventParticipationLimitException;
import ru.yandex.practicum.users.errors.NotPublishedEventParticipationException;
import ru.yandex.practicum.users.errors.RepeatParticipationRequestException;
import ru.yandex.practicum.users.model.ParticipationRequestStatus;
import ru.yandex.practicum.users.model.User;
import ru.yandex.practicum.users.repository.UserRepository;
import ru.yandex.practicum.users.service.ParticipationRequestService;
import ru.yandex.practicum.users.service.PrivateUserEventService;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@Slf4j
@TestPropertySource(locations = "classpath:application-test.properties")
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

    @MockBean
    UserServiceFeign userServiceFeign;

    private User eventOwner;
    private User eventParticipant;
    private User eventSecondParticipant;
    private EventFullDto pendingEvent;

    @BeforeEach
    void setUp() {
        eventOwner = new User();
        eventOwner.setId(1L);
        eventOwner.setName("Test User");
        eventOwner.setEmail("eventOwner@example.com");
        //eventOwner = userRepository.save(eventOwner);

        eventParticipant = new User();
        eventParticipant.setId(2L);
        eventParticipant.setName("Test User");
        eventParticipant.setEmail("eventParticipant@example.com");
        //eventParticipant = userRepository.save(eventParticipant);

        eventSecondParticipant = new User();
        eventSecondParticipant.setId(3L);
        eventSecondParticipant.setName("Test User");
        eventSecondParticipant.setEmail("eventSecondParticipant@example.com");
        //eventSecondParticipant = userRepository.save(eventSecondParticipant);

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
        newEventDto.setLocation(LocationMapper.mapLocationToDto(location));
        pendingEvent = eventService.addNewEvent(eventOwner.getId(), newEventDto);
    }

    @Test
    void getUserRequests_ShouldReturnEmptyListInitially() {
        List<ParticipationRequestDto> requests = participationRequestService.getUserRequests(eventOwner.getId());
        assertThat(requests).isEmpty();
    }

    @Test
    void addParticipationRequest_ShouldCreateNewRequest() {
        UserDto mockUser = UserDto.builder()
                .id(eventParticipant.getId())
                .name(eventParticipant.getName())
                .email(eventParticipant.getEmail())
                .build();
        Mockito.when(userServiceFeign.getUserById(Mockito.anyLong()))
                .thenReturn(ResponseEntity.ok(mockUser));
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
    void addParticipationRequest_ShouldThrowError() {
        Mockito.when(userServiceFeign.getUserById(eventOwner.getId()))
                .thenReturn(ResponseEntity.ok(
                        UserDto.builder()
                                .id(eventOwner.getId())
                                .name(eventOwner.getName())
                                .email(eventOwner.getEmail())
                                .build()
                ));

        Mockito.when(userServiceFeign.getUserById(eventParticipant.getId()))
                .thenReturn(ResponseEntity.ok(
                        UserDto.builder()
                                .id(eventParticipant.getId())
                                .name(eventParticipant.getName())
                                .email(eventParticipant.getEmail())
                                .build()
                ));

        Mockito.when(userServiceFeign.getUserById(eventSecondParticipant.getId()))
                .thenReturn(ResponseEntity.ok(
                        UserDto.builder()
                                .id(eventSecondParticipant.getId())
                                .name(eventSecondParticipant.getName())
                                .email(eventSecondParticipant.getEmail())
                                .build()
                ));
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
    void cancelRequest_ShouldChangeRequestStatusToCanceled() {
        UserDto mockUser = UserDto.builder()
                .id(eventParticipant.getId())
                .name(eventParticipant.getName())
                .email(eventParticipant.getEmail())
                .build();
        Mockito.when(userServiceFeign.getUserById(Mockito.anyLong()))
                .thenReturn(ResponseEntity.ok(mockUser));
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
