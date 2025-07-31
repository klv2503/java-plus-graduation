package ru.practicum.events;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.MainService;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.events.controller.PublicEventController;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.dto.LookEventDto;
import ru.practicum.events.dto.SearchEventsParams;
import ru.practicum.events.model.Location;
import ru.practicum.events.model.StateEvent;
import ru.practicum.events.service.PublicEventsService;
import ru.practicum.users.dto.UserShortDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicEventController.class)
@ContextConfiguration(classes = MainService.class)
@RequiredArgsConstructor
public class PublicEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PublicEventsService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String firstDate = "2024-12-10 14:30:00";

    private final String secondDate = "2025-03-10 14:30:00";

    private final String thirdDate = "2024-12-11 14:30:00";

    EventFullDto eventFullDto = EventFullDto.builder()
            .id(1L)
            .annotation("12345".repeat(5))
            .category(new CategoryDto())
            .eventDate(firstDate)
            .confirmedRequests(0)
            .paid(true)
            .title("without")
            .initiator(new UserShortDto())
            .views(0)
            .createdOn(secondDate)
            .description("12345".repeat(15))
            .publishedOn(thirdDate)
            .location(new Location())
            .participantLimit(0)
            .requestModeration(true)
            .state(StateEvent.PUBLISHED)
            .build();

    EventShortDto eventShortDto = EventShortDto.builder()
            .annotation("12345".repeat(5))
            .category(new CategoryDto())
            .confirmedRequests(0)
            .eventDate(firstDate)
            .initiator(new UserShortDto())
            .paid(true)
            .title("without")
            .build();

    @Test
    @SneakyThrows
    public void getEventInfo_whenValidParams_thenGetResponse() {
        when(service.getEventInfo(ArgumentMatchers.any())).thenReturn(eventFullDto);
        long id = 1L;

        RequestBuilder request = MockMvcRequestBuilders
                .get("/events/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = mockMvc.perform(request).andExpect(status().isOk()).andReturn();
        assertNotNull(mvcResult.getResponse());

        ArgumentCaptor<LookEventDto> lookEventDtoCaptor = ArgumentCaptor.forClass(LookEventDto.class);

        verify(service).getEventInfo(lookEventDtoCaptor.capture());
        assertEquals(1L, lookEventDtoCaptor.getValue().getId());
        assertNotNull(lookEventDtoCaptor.getValue().getIp());
        assertNotNull(lookEventDtoCaptor.getValue().getUri());

    }

    @Test
    @SneakyThrows
    public void getFilteredEvents_whenCallMethod_thenGetResponse() {
        List<EventShortDto> expectedList = List.of(eventShortDto);

        ArgumentCaptor<SearchEventsParams> searchParam = ArgumentCaptor.forClass(SearchEventsParams.class);
        ArgumentCaptor<LookEventDto> lookEventDtoCaptor = ArgumentCaptor.forClass(LookEventDto.class);
        when(service.getFilteredEvents(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(expectedList);

        RequestBuilder request = MockMvcRequestBuilders
                .get("/events")
                .accept(MediaType.APPLICATION_JSON)
                .param("rangeStart", thirdDate)
                .param("rangeEnd", secondDate)
                .contentType(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = mockMvc.perform(request).andExpect(status().isOk()).andReturn();
        assertNotNull(mvcResult.getResponse());

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<EventShortDto> factList = objectMapper.readValue(jsonResponse, new TypeReference<>() {
        });
        assertEquals(1, factList.size());
        assertEquals(eventShortDto, factList.getFirst());


        verify(service).getFilteredEvents(searchParam.capture(), lookEventDtoCaptor.capture());

        assertEquals(null, lookEventDtoCaptor.getValue().getId());
        assertNotNull(lookEventDtoCaptor.getValue().getIp());
        assertNotNull(lookEventDtoCaptor.getValue().getUri());

        assertEquals("", searchParam.getValue().getText(), "text");
        assertTrue(searchParam.getValue().getCategories().isEmpty(), "categories");
        assertNull(searchParam.getValue().getPaid(), "paid");
        assertEquals(thirdDate, searchParam.getValue().getRangeStart(), "rangeStart");
        assertEquals(secondDate, searchParam.getValue().getRangeEnd(), "rangeEnd");
        assertFalse(searchParam.getValue().getOnlyAvailable(), "onlyAvailable");
        assertEquals("EVENT_DATE", searchParam.getValue().getSort(), "sort");
        assertEquals(0, searchParam.getValue().getFrom(), "from");
        assertEquals(10, searchParam.getValue().getSize(), "size");

    }

    @Test
    @SneakyThrows
    public void getFilteredEvents_whenCallMethodWithInvalidDate_thenThrow() {
        List<EventShortDto> expectedList = List.of(eventShortDto);

        when(service.getFilteredEvents(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(expectedList);

        RequestBuilder request = MockMvcRequestBuilders
                .get("/events")
                .accept(MediaType.APPLICATION_JSON)
                .param("rangeStart", secondDate)
                .param("rangeEnd", thirdDate)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request).andExpect(status().isBadRequest());
    }
}
