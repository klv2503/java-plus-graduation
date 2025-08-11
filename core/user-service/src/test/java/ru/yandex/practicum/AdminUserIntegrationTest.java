package ru.yandex.practicum;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.GetUsersDto;
import ru.yandex.practicum.dto.NewUserRequest;
import ru.yandex.practicum.dto.user.UserDto;
import ru.yandex.practicum.repository.AdminUserRepository;
import ru.yandex.practicum.service.AdminUserService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Slf4j
@TestPropertySource(locations = "classpath:application-test.properties")
public class AdminUserIntegrationTest {
    @Autowired
    private AdminUserService adminUserService;
    @Autowired
    private AdminUserRepository adminUserRepository;

    @Test
    public void createUserTest() {
        NewUserRequest newUserRequest = new NewUserRequest("username", "username@host.com");
        UserDto userDto = adminUserService.addUser(newUserRequest);

        assertAll(
                () -> Assertions.assertEquals(newUserRequest.getName(), userDto.getName()),
                () -> Assertions.assertEquals(newUserRequest.getEmail(), userDto.getEmail()),
                () -> Assertions.assertNotEquals(null, userDto.getId())
        );

    }

    @Test
    public void getUserSizeTest() {
        GetUsersDto params = new GetUsersDto(Collections.emptyList(), 0, 10);
        List<UserDto> listUserDto = adminUserService.getUsers(params);

        assertEquals(10, listUserDto.size());
    }

    @Test
    public void getUserFromTest() {
        GetUsersDto params = new GetUsersDto(Collections.emptyList(), 5, 2);
        List<UserDto> listUserDto = adminUserService.getUsers(params);

        assertAll(
                () -> assertEquals(2, listUserDto.size()),
                () -> Assertions.assertEquals(5, listUserDto.getFirst().getId())
        );
    }

    @Test
    public void getUserIdsTest() {
        GetUsersDto params = new GetUsersDto(List.of(2L, 3L, 9L), 0, 10);
        List<UserDto> listUserDto = adminUserService.getUsers(params);

        assertEquals(3, listUserDto.size());
    }

    @Test
    public void deleteUserTest() {
        adminUserService.deleteUser(12L);
        Assertions.assertFalse(adminUserRepository.existsById(12L));
    }
}