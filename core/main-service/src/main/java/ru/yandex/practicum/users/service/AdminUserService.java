package ru.yandex.practicum.users.service;

import ru.yandex.practicum.users.dto.GetUsersDto;
import ru.yandex.practicum.users.dto.NewUserRequest;
import ru.yandex.practicum.users.dto.UserDto;
import ru.yandex.practicum.users.model.User;

import java.util.List;

public interface AdminUserService {

    List<UserDto> getUsers(GetUsersDto parameters);

    UserDto addUser(NewUserRequest user);

    void deleteUser(Long id);

    User getUser(long id);
}
