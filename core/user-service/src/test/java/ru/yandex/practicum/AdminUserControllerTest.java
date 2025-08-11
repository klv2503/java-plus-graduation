package ru.yandex.practicum;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.yandex.practicum.controller.AdminUserController;
import ru.yandex.practicum.dto.GetUsersDto;
import ru.yandex.practicum.dto.NewUserRequest;
import ru.yandex.practicum.dto.user.UserDto;
import ru.yandex.practicum.service.AdminUserService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminUserController.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class AdminUserControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    AdminUserService adminUserService;
    UserDto userDto = new UserDto(5L, "User", "User@user.com");
    UserDto userDtoSecond = new UserDto(12L, "User1", "User1@user.com");
    NewUserRequest newUserRequest = new NewUserRequest("User", "User@user.com");

    @Test
    @SneakyThrows
    public void addUserTest() {
        when(adminUserService.addUser(any(NewUserRequest.class))).thenReturn(userDto);

        mockMvc.perform(post("/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUserRequest))
                .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(userDto.getId()), Long.class))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(userDto.getName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email", Matchers.is(userDto.getEmail())));
    }

    @Test
    @SneakyThrows
    public void getUsersTest() {
        when(adminUserService.getUsers(any(GetUsersDto.class))).thenReturn(List.of(userDto, userDtoSecond));

        mockMvc.perform(get("/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].id", Matchers.is(userDto.getId()), Long.class))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].name", Matchers.is(userDto.getName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].email", Matchers.is(userDto.getEmail())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].id", Matchers.is(userDtoSecond.getId()), Long.class))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].name", Matchers.is(userDtoSecond.getName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].email", Matchers.is(userDtoSecond.getEmail())));
    }

    @Test
    @SneakyThrows
    public void deleteUserTest() {
        mockMvc.perform(delete("/admin/users/" + userDto.getId()))
                .andExpect(status().isNoContent());

        verify(adminUserService, times(1))
                .deleteUser(userDto.getId());
    }
}
