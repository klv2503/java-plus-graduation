package ru.practicum.users;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.MainService;
import ru.practicum.users.dto.GetUsersDto;
import ru.practicum.users.dto.NewUserRequest;
import ru.practicum.users.dto.UserDto;
import ru.practicum.users.repository.AdminUserRepository;
import ru.practicum.users.service.AdminUserService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "classpath:application-test.properties",
        classes = MainService.class)
@ExtendWith(SpringExtension.class)
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class AdminUserIntegrationTest {
    @Autowired
    private AdminUserService adminUserService;
    @Autowired
    private AdminUserRepository adminUserRepository;

    @Test
    @Transactional
    public void createUserTest() {
        NewUserRequest newUserRequest = new NewUserRequest("username", "username@host.com");
        UserDto userDto = adminUserService.addUser(newUserRequest);

        assertAll(
                () -> assertEquals(newUserRequest.getName(), userDto.getName()),
                () -> assertEquals(newUserRequest.getEmail(), userDto.getEmail()),
                () -> assertNotEquals(null, userDto.getId())
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
                () -> assertEquals(5, listUserDto.getFirst().getId())
        );
    }

    @Test
    public void getUserIdsTest() {
        GetUsersDto params = new GetUsersDto(List.of(2L, 3L, 9L), 0, 10);
        List<UserDto> listUserDto = adminUserService.getUsers(params);

        assertEquals(3, listUserDto.size());
    }

    @Test
    @Transactional
    public void deleteUserTest() {
        adminUserService.deleteUser(12L);
        assertFalse(adminUserRepository.existsById(12L));
    }
}