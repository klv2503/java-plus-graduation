package ru.practicum.events;

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
import ru.practicum.events.controller.AdminEventController;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.UpdateEventAdminRequest;
import ru.practicum.events.model.StateEvent;
import ru.practicum.events.service.AdminEventService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = AdminEventController.class)
@ContextConfiguration(classes = MainService.class)
public class AdminEventControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AdminEventService adminEventService;

    private final EventFullDto eventDto = EventFullDto.builder()
            .id(1L)
            .title("Test Event")
            .annotation("Test Annotation")
            .description("Detailed description")
            .eventDate(LocalDateTime.now().plusDays(1).toString())
            .paid(false)
            .participantLimit(100)
            .confirmedRequests(10)
            .views(200)
            .state(StateEvent.PENDING)
            .createdOn(LocalDateTime.now().toString())
            .publishedOn(LocalDateTime.now().plusHours(1).toString())
            .build();

    private final UpdateEventAdminRequest updateEventRequest = UpdateEventAdminRequest.builder()
            .title("Updated Title")
            .description("12345".repeat(5))
            .eventDate(LocalDateTime.now().plusDays(2))
            .paid(false)
            .participantLimit(50)
            .build();

    @Test
    @SneakyThrows
    public void getEventsTest() {
        when(adminEventService.getEvents(any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(eventDto));

        mockMvc.perform(get("/admin/events")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(eventDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].title", is(eventDto.getTitle())))
                .andExpect(jsonPath("$.[0].annotation", is(eventDto.getAnnotation())))
                .andExpect(jsonPath("$.[0].description", is(eventDto.getDescription())))
                .andExpect(jsonPath("$.[0].eventDate", is(eventDto.getEventDate())))
                .andExpect(jsonPath("$.[0].paid", is(eventDto.isPaid())))
                .andExpect(jsonPath("$.[0].participantLimit", is(eventDto.getParticipantLimit())))
                .andExpect(jsonPath("$.[0].confirmedRequests", is(eventDto.getConfirmedRequests())))
                .andExpect(jsonPath("$.[0].views", is(eventDto.getViews())))
                .andExpect(jsonPath("$.[0].state", is(eventDto.getState().toString())))
                .andExpect(jsonPath("$.[0].createdOn", is(eventDto.getCreatedOn())))
                .andExpect(jsonPath("$.[0].publishedOn", is(eventDto.getPublishedOn())));
    }

    @Test
    @SneakyThrows
    public void updateEventTest() {
        when(adminEventService.updateEvent(anyLong(), any(UpdateEventAdminRequest.class)))
                .thenReturn(eventDto);

        mockMvc.perform(patch("/admin/events/{eventId}", eventDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventRequest))
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(eventDto.getId()), Long.class))
                .andExpect(jsonPath("$.title", is(eventDto.getTitle())))
                .andExpect(jsonPath("$.annotation", is(eventDto.getAnnotation())))
                .andExpect(jsonPath("$.description", is(eventDto.getDescription())))
                .andExpect(jsonPath("$.eventDate", is(eventDto.getEventDate())))
                .andExpect(jsonPath("$.paid", is(eventDto.isPaid())))
                .andExpect(jsonPath("$.participantLimit", is(eventDto.getParticipantLimit())))
                .andExpect(jsonPath("$.confirmedRequests", is(eventDto.getConfirmedRequests())))
                .andExpect(jsonPath("$.views", is(eventDto.getViews())))
                .andExpect(jsonPath("$.state", is(eventDto.getState().toString())))
                .andExpect(jsonPath("$.createdOn", is(eventDto.getCreatedOn())))
                .andExpect(jsonPath("$.publishedOn", is(eventDto.getPublishedOn())));
    }
}
