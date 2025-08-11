package ru.yandex.practicum.mapper;

import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.NewUserRequest;
import ru.yandex.practicum.model.User;

@Component
public class NewUserRequestToUserMapper {

    public User mapNewUserRequestToUser(NewUserRequest newUserRequest) {
        User user = new User();
        if (!Strings.isBlank(newUserRequest.getName()))
            user.setName(newUserRequest.getName());
        if (!Strings.isBlank(newUserRequest.getEmail()))
            user.setEmail(newUserRequest.getEmail());
        return user;
    }

}
