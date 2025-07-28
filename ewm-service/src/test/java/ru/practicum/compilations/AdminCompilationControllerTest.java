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
import ru.practicum.compilations.controller.AdminCompilationController;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.NewCompilationDto;
import ru.practicum.compilations.dto.UpdateCompilationRequest;
import ru.practicum.compilations.service.CompilationService;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.users.dto.UserShortDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = AdminCompilationController.class)
@ContextConfiguration(classes = MainService.class)
public class AdminCompilationControllerTest {
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
    private final UpdateCompilationRequest updateCompilationRequest = new UpdateCompilationRequest(Set.of(1L, 2L), true,
            "title");

    @Test
    @SneakyThrows
    public void postTest() {
        when(compilationService.add(any(NewCompilationDto.class))).thenReturn(compilationDto);

        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCompilationDto))
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.events.[0].id", is(eventShortDto.getId()), Long.class))
                .andExpect(jsonPath("$.id", is(compilationDto.getId()), Long.class))
                .andExpect(jsonPath("$.pinned", is(compilationDto.getPinned())))
                .andExpect(jsonPath("$.title", is(compilationDto.getTitle())));
    }

    @Test
    @SneakyThrows
    public void updateTest() {
        when(compilationService.update(any(Long.class), any(UpdateCompilationRequest.class))).thenReturn(compilationDto);

        mockMvc.perform(patch("/admin/compilations/" + compilationDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCompilationRequest))
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events.[0].id", is(eventShortDto.getId()), Long.class))
                .andExpect(jsonPath("$.id", is(compilationDto.getId()), Long.class))
                .andExpect(jsonPath("$.pinned", is(compilationDto.getPinned())))
                .andExpect(jsonPath("$.title", is(compilationDto.getTitle())));
    }

    @Test
    @SneakyThrows
    public void deleteTest() {
        mockMvc.perform(delete("/admin/compilations/" + compilationDto.getId()))
                .andExpect(status().isNoContent());
    }
}
