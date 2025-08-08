package ru.yandex.practicum.comments;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.comments.controller.AdminCommentController;
import ru.yandex.practicum.comments.service.CommentService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminCommentController.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
public class AdminCommentsControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    CommentService commentService;

    @Test
    @SneakyThrows
    public void deleteByIdTest() {
        mockMvc.perform(delete("/admin/comments/" + 1L))
                .andExpect(status().isNoContent());
    }
}
