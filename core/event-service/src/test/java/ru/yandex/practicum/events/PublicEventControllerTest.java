package ru.yandex.practicum.events;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.yandex.practicum.dto.location.LocationDto;
import ru.yandex.practicum.events.controller.PublicEventController;
import ru.yandex.practicum.dto.events.EventFullDto;
import ru.yandex.practicum.dto.events.EventShortDto;
import ru.yandex.practicum.events.dto.LookEventDto;
import ru.yandex.practicum.events.dto.SearchEventsParams;
import ru.yandex.practicum.enums.StateEvent;
import ru.yandex.practicum.events.service.PublicEventsService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicEventController.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class PublicEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PublicEventsService service;

    @Autowired
    private ObjectMapper objectMapper;

    private final String firstDate = "2024-12-10 14:30:00";

    private final String secondDate = "2025-03-10 14:30:00";

    private final String thirdDate = "2024-12-11 14:30:00";

    EventFullDto eventFullDto = EventFullDto.builder()
            .id(1L)
            .annotation("12345".repeat(5))
            .category(1L)
            .eventDate(firstDate)
            .confirmedRequests(0)
            .paid(true)
            .title("without")
            .initiator(0L)
            .views(0)
            .createdOn(secondDate)
            .description("12345".repeat(15))
            .publishedOn(thirdDate)
            .location(new LocationDto())
            .participantLimit(0)
            .requestModeration(true)
            .state(StateEvent.PUBLISHED)
            .build();

    EventShortDto eventShortDto = EventShortDto.builder()
            .annotation("12345".repeat(5))
            .category(1L)
            .confirmedRequests(0)
            .eventDate(firstDate)
            .initiator(0L)
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
        Assertions.assertEquals(1L, lookEventDtoCaptor.getValue().getId());
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

        Assertions.assertEquals(null, lookEventDtoCaptor.getValue().getId());
        assertNotNull(lookEventDtoCaptor.getValue().getIp());
        assertNotNull(lookEventDtoCaptor.getValue().getUri());

        Assertions.assertEquals("", searchParam.getValue().getText(), "text");
        Assertions.assertTrue(searchParam.getValue().getCategories().isEmpty(), "categories");
        Assertions.assertNull(searchParam.getValue().getPaid(), "paid");
        Assertions.assertEquals(thirdDate, searchParam.getValue().getRangeStart(), "rangeStart");
        Assertions.assertEquals(secondDate, searchParam.getValue().getRangeEnd(), "rangeEnd");
        Assertions.assertFalse(searchParam.getValue().getOnlyAvailable(), "onlyAvailable");
        Assertions.assertEquals("EVENT_DATE", searchParam.getValue().getSort(), "sort");
        Assertions.assertEquals(0, searchParam.getValue().getFrom(), "from");
        Assertions.assertEquals(10, searchParam.getValue().getSize(), "size");

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
