package ru.practicum.compilations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewCompilationDto {
    private Set<Long> events;
    private Boolean pinned;
    @NotBlank(message = "Field: title. Error: must not be blank. Value: empty")
    @Size(max = 50, message = "Field: title. Error: length must be between 1 and 50 symbols.")
    private String title;
}
