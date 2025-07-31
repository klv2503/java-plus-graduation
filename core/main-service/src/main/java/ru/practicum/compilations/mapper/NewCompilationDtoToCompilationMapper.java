package ru.practicum.compilations.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.compilations.dto.NewCompilationDto;
import ru.practicum.compilations.model.Compilation;

@Component
public class NewCompilationDtoToCompilationMapper {
    public static Compilation mapNewCompilationDtoToCompilation(NewCompilationDto newCompilationDto) {
        return new Compilation(null,
                newCompilationDto.getEvents(),
                (newCompilationDto.getPinned() != null) ? newCompilationDto.getPinned() : false,
                newCompilationDto.getTitle());
    }
}
