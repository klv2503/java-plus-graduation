package ru.practicum.comments;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.MainService;
import ru.practicum.comments.controller.UserCommentController;
import ru.practicum.comments.service.CommentService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = UserCommentController.class)
@ContextConfiguration(classes = MainService.class)
public class UserCommentControllerTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    CommentService commentService;

    @Test
    @SneakyThrows
    public void softDeleteTest() {
        mockMvc.perform(delete("/users/" + 2L + "/comments/" + 1L))
                .andExpect(status().isNoContent());
    }
}
