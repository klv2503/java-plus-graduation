package ru.practicum.users;

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
import ru.practicum.users.controller.AdminUserController;
import ru.practicum.users.dto.GetUsersDto;
import ru.practicum.users.dto.NewUserRequest;
import ru.practicum.users.dto.UserDto;
import ru.practicum.users.service.AdminUserService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = AdminUserController.class)
@ContextConfiguration(classes = MainService.class)
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
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
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
                .andExpect(jsonPath("$.[0].id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].name", is(userDto.getName())))
                .andExpect(jsonPath("$.[0].email", is(userDto.getEmail())))
                .andExpect(jsonPath("$.[1].id", is(userDtoSecond.getId()), Long.class))
                .andExpect(jsonPath("$.[1].name", is(userDtoSecond.getName())))
                .andExpect(jsonPath("$.[1].email", is(userDtoSecond.getEmail())));
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
