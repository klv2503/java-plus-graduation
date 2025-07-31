package ru.practicum.users;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.MainService;
import ru.practicum.users.controller.PrivateRequestsForParticipation;
import ru.practicum.users.dto.ParticipationRequestDto;
import ru.practicum.users.model.ParticipationRequestStatus;
import ru.practicum.users.service.ParticipationRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = PrivateRequestsForParticipation.class)
@ContextConfiguration(classes = MainService.class)
public class PrivateRequestsForParticipationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ParticipationRequestService participationRequestService;

    private final Long userId = 1L;
    private final Long eventId = 2L;
    private final Long requestId = 3L;

    @Test
    void getUserRequests_ShouldReturnListOfRequests() throws Exception {
        List<ParticipationRequestDto> requests = List.of(
                new ParticipationRequestDto(requestId, userId, eventId, ParticipationRequestStatus.PENDING, LocalDateTime.now()));

        Mockito.when(participationRequestService.getUserRequests(userId)).thenReturn(requests);

        mockMvc.perform(get("/users/{userId}/requests", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(requests.size()))
                .andExpect(jsonPath("$[0].id").value(requestId))
                .andExpect(jsonPath("$[0].requester").value(userId))
                .andExpect(jsonPath("$[0].event").value(eventId))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void addParticipationRequest_ShouldReturnCreatedRequest() throws Exception {
        ParticipationRequestDto requestDto =
                new ParticipationRequestDto(requestId, userId, eventId, ParticipationRequestStatus.CONFIRMED, LocalDateTime.now());

        Mockito.when(participationRequestService.addParticipationRequest(userId, eventId)).thenReturn(requestDto);

        mockMvc.perform(post("/users/{userId}/requests", userId)
                        .param("eventId", eventId.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.requester").value(userId))
                .andExpect(jsonPath("$.event").value(eventId))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void cancelRequest_ShouldReturnCanceledRequest() throws Exception {
        ParticipationRequestDto canceledRequest =
                new ParticipationRequestDto(requestId, userId, eventId, ParticipationRequestStatus.CANCELED, LocalDateTime.now());

        Mockito.when(participationRequestService.cancelRequest(userId, requestId)).thenReturn(canceledRequest);

        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", userId, requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.requester").value(userId))
                .andExpect(jsonPath("$.event").value(eventId))
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }
}
