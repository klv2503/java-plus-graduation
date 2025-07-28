package ru.practicum.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewUserRequest {

    @NotBlank(message = "Field: name. Error: must not be blank. Value: empty")
    @Size(min = 2, max = 250, message = "Field: name. Error: length must be between 2 and 250 symbols")
    private String name;

    @Email(message = "Field: email. Error: must be correct email.")
    @NotBlank(message = "Field: email. Error: must not be blank. Value: empty")
    @Size(min = 6, max = 254, message = "Field: email. Error: length must be between 6 and  254 symbols.")
    private String email;

}
