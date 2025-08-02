package ru.practicum.compilations;

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
import ru.practicum.compilations.controller.PublicCompilationController;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.Filter;
import ru.practicum.compilations.dto.NewCompilationDto;
import ru.practicum.compilations.service.CompilationService;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.users.dto.UserShortDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = PublicCompilationController.class)
@ContextConfiguration(classes = MainService.class)
public class PublicCompilationControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CompilationService compilationService;

    private final UserShortDto userShortDto = new UserShortDto(3L, "username");
    private final EventShortDto eventShortDto = new EventShortDto(1L, "annotation", null, 5,
            LocalDateTime.of(2025, 11, 7, 12, 30, 0).toString(), userShortDto, false,
            "title", 15);
    private final CompilationDto compilationDto = new CompilationDto(List.of(eventShortDto), 2L, true, "title");
    private final NewCompilationDto newCompilationDto = new NewCompilationDto(Set.of(1L, 2L), false, "title");

    @Test
    @SneakyThrows
    public void getByIdTest() {
        when(compilationService.getById(anyLong())).thenReturn(compilationDto);

        mockMvc.perform(get("/compilations/" + compilationDto.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events.[0].id", is(eventShortDto.getId()), Long.class))
                .andExpect(jsonPath("$.id", is(compilationDto.getId()), Long.class))
                .andExpect(jsonPath("$.pinned", is(compilationDto.getPinned())))
                .andExpect(jsonPath("$.title", is(compilationDto.getTitle())));
    }

    @Test
    @SneakyThrows
    public void getTest() {
        when(compilationService.get(any(Filter.class))).thenReturn(List.of(compilationDto));

        mockMvc.perform(get("/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].events.[0].id", is(eventShortDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].id", is(compilationDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].pinned", is(compilationDto.getPinned())))
                .andExpect(jsonPath("$.[0].title", is(compilationDto.getTitle())));
    }
}
