package ru.practicum.users.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.users.dto.GetUsersDto;
import ru.practicum.users.dto.NewUserRequest;
import ru.practicum.users.dto.UserDto;
import ru.practicum.users.service.AdminUserService;

import java.util.List;

@RestController
@RequestMapping(path = "/admin/users")
@Validated
@Slf4j
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @PostMapping
    public ResponseEntity<UserDto> addUser(@RequestBody @Valid NewUserRequest newUser) {
        log.info("\nRequest for adding of new user {}", newUser);
        UserDto createdUser = adminUserService.addUser(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getUsers(@RequestParam(required = false, defaultValue = "") List<Long> ids,
                                                  @RequestParam(required = false, defaultValue = "0") int from,
                                                  @RequestParam(required = false, defaultValue = "10") int size) {
        log.info("Accepted request for get users {}, {}, {}", from, size, ids);
        GetUsersDto parameters = new GetUsersDto(ids, from, size);
        List<UserDto> response = adminUserService.getUsers(parameters);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable
                                             @Min(value = 1, message = "ID must be positive") Long userId) {
        log.info("Accepted request for deleting user {}", userId);
        adminUserService.deleteUser(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}
