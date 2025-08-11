package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.GetUsersDto;
import ru.yandex.practicum.dto.NewUserRequest;
import ru.yandex.practicum.dto.user.UserDto;
import ru.yandex.practicum.model.User;

import java.util.List;

public interface AdminUserService {

    List<UserDto> getUsers(GetUsersDto parameters);

    UserDto addUser(NewUserRequest user);

    void deleteUser(Long id);

    User getUser(long id);
}
