package ru.practicum.events.private_events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.MainService;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.dto.NewEventDto;
import ru.practicum.events.dto.UpdateEventUserRequest;
import ru.practicum.events.model.Location;
import ru.practicum.events.model.StateEvent;
import ru.practicum.users.controller.PrivateUserEventController;
import ru.practicum.users.dto.UserShortDto;
import ru.practicum.users.service.PrivateUserEventService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = PrivateUserEventController.class)
@ContextConfiguration(classes = MainService.class)
public class PrivateUserEventsControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private PrivateUserEventService privateUserEventService;
    private final NewEventDto eventDto = new NewEventDto(1L, "annotationannotationannotation", 1L, "descriptiondescriptiondescriptiondescriptiondescriptiondescriptiondescriptiondescriptiondescriptiondescriptiondescriptiondescription",
            "2025-12-31 15:10:05", new Location(), true, 10, false, "Title");
    private final EventFullDto eventFullDto = EventFullDto.builder()
            .id(1L)
            .location(new Location(1L, 33, 33))
            .eventDate("2024-12-31 15:10:05")
            .publishedOn("2024-12-31 15:10:05")
            .annotation("annotation2")
            .description("descr")
            .title("Title")
            .state(StateEvent.PENDING)
            .initiator(new UserShortDto())
            .views(1)
            .confirmedRequests(1)
            .paid(true)
            .requestModeration(false)
            .createdOn("2024-12-31 15:10:05")
            .build();
    private final EventShortDto eventShortDto = EventShortDto.builder()
            .annotation("anon")
            .category(new CategoryDto())
            .confirmedRequests(1)
            .eventDate("2024-12-31 15:10:05")
            .id(1L)
            .initiator(new UserShortDto())
            .paid(true)
            .title("Title")
            .views(1)
            .build();

    @Test
    @SneakyThrows
    public void addUserEvent() {
        when(privateUserEventService.addNewEvent(any(), any())).thenReturn(eventFullDto);

        mockMvc.perform(post("/users/{userId}/events", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto))
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(eventFullDto.getId()), Long.class))
                .andExpect(jsonPath("$.eventDate", is(eventFullDto.getEventDate())))
                .andExpect(jsonPath("$.publishedOn", is(eventFullDto.getPublishedOn())))
                .andExpect(jsonPath("$.annotation", is(eventFullDto.getAnnotation())))
                .andExpect(jsonPath("$.description", is(eventFullDto.getDescription())))
                .andExpect(jsonPath("$.title", is(eventFullDto.getTitle())))
                .andExpect(jsonPath("$.state", is(eventFullDto.getState().toString())))
                .andExpect(jsonPath("$.views", is(eventFullDto.getViews())))
                .andExpect(jsonPath("$.confirmedRequests", is(eventFullDto.getConfirmedRequests())))
                .andExpect(jsonPath("$.paid", is(eventFullDto.isPaid())))
                .andExpect(jsonPath("$.requestModeration", is(eventFullDto.isRequestModeration())))
                .andExpect(jsonPath("$.createdOn", is(eventFullDto.getCreatedOn())));
    }

    @Test
    @SneakyThrows
    public void testGetUserEventById() {
        when(privateUserEventService.getUserEventById(any(), any())).thenReturn(eventFullDto);

        mockMvc.perform(get("/users/{userId}/events/{eventId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(eventFullDto.getId()), Long.class))
                .andExpect(jsonPath("$.eventDate", is(eventFullDto.getEventDate())))
                .andExpect(jsonPath("$.publishedOn", is(eventFullDto.getPublishedOn())))
                .andExpect(jsonPath("$.annotation", is(eventFullDto.getAnnotation())))
                .andExpect(jsonPath("$.description", is(eventFullDto.getDescription())))
                .andExpect(jsonPath("$.title", is(eventFullDto.getTitle())))
                .andExpect(jsonPath("$.state", is(eventFullDto.getState().toString())))
                .andExpect(jsonPath("$.views", is(eventFullDto.getViews())))
                .andExpect(jsonPath("$.confirmedRequests", is(eventFullDto.getConfirmedRequests())))
                .andExpect(jsonPath("$.paid", is(eventFullDto.isPaid())))
                .andExpect(jsonPath("$.requestModeration", is(eventFullDto.isRequestModeration())))
                .andExpect(jsonPath("$.createdOn", is(eventFullDto.getCreatedOn())));
    }

    @Test
    @SneakyThrows
    public void testGetAllUsersEvents() {
        when(privateUserEventService.getUsersEvents(any())).thenReturn(List.of(eventShortDto));

        mockMvc.perform(get("/users/{userId}/events", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id", is(eventShortDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].eventDate", is(eventShortDto.getEventDate())))
                .andExpect(jsonPath("$[0].annotation", is(eventShortDto.getAnnotation())))
                .andExpect(jsonPath("$[0].title", is(eventShortDto.getTitle())))
                .andExpect(jsonPath("$[0].views", is(eventShortDto.getViews())))
                .andExpect(jsonPath("$[0].confirmedRequests", is(eventShortDto.getConfirmedRequests())))
                .andExpect(jsonPath("$[0].paid", is(eventShortDto.isPaid())));
    }

    @Test
    @SneakyThrows
    public void updateUserEvent() {
        UpdateEventUserRequest updateDto = new UpdateEventUserRequest(1L, "aninaninaninaninaninaninaninanin", 1, "descdescdescdescdescdescdescdescdescdescdescdescdescdescdescdescdescdescdescdescdescdesc",
                "2026-03-11 15:10:00", new Location(1L, 33, 33), true,
                1, true, "S", "titile");

        when(privateUserEventService.updateUserEvent(any(), anyLong(), any())).thenReturn(eventFullDto);

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(eventFullDto.getId()), Long.class))
                .andExpect(jsonPath("$.eventDate", is(eventFullDto.getEventDate())))
                .andExpect(jsonPath("$.annotation", is(eventFullDto.getAnnotation())))
                .andExpect(jsonPath("$.title", is(eventFullDto.getTitle())))
                .andExpect(jsonPath("$.paid", is(eventFullDto.isPaid())));
    }
}
