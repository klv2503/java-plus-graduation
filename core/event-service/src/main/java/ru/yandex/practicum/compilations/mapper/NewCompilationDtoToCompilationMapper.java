package ru.yandex.practicum.compilations.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.compilations.dto.NewCompilationDto;
import ru.yandex.practicum.compilations.model.Compilation;

@Component
public class NewCompilationDtoToCompilationMapper {
    public static Compilation mapNewCompilationDtoToCompilation(NewCompilationDto newCompilationDto) {
        return new Compilation(null,
                newCompilationDto.getEvents(),
                (newCompilationDto.getPinned() != null) ? newCompilationDto.getPinned() : false,
                newCompilationDto.getTitle());
    }
}
