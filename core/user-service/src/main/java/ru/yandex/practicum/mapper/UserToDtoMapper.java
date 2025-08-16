package ru.yandex.practicum.mapper;

import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.user.UserDto;
import ru.yandex.practicum.model.User;

import java.util.List;

@Component
public class UserToDtoMapper {

    public static User mapUserDtoToUser(UserDto userDto) {
        User user = new User();
        if (!Strings.isBlank(userDto.getName()))
            user.setName(userDto.getName());
        if (!Strings.isBlank(userDto.getEmail()))
            user.setEmail(userDto.getEmail());
        return user;
    }

    public static UserDto mapUserToUserDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }

    //List<User> mapDtoListToUsersList(List<UserDto> userDtos);

    public List<UserDto> mapUsersListToDtoList(List<User> users) {
        return users.stream()
                .map(UserToDtoMapper::mapUserToUserDto)
                .toList();
    }

}
