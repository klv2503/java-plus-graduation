package ru.practicum.compilations.mapper;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.model.Compilation;
import ru.practicum.events.dto.EventShortDto;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@Component
@NoArgsConstructor
public class CompilationToCompilationDto {
    public static CompilationDto mapCompilationToCompilationDto(Compilation compilation,
                                                                List<EventShortDto> events) {
        return new CompilationDto(
                events,
                compilation.getId(),
                compilation.getPinned(),
                compilation.getTitle());
    }

    public static List<CompilationDto> mapToListCompilationDto(List<Compilation> compilationList,
                                                               HashMap<Long, EventShortDto> events) {
        return compilationList.stream()
                .map(compilation -> mapCompilationToCompilationDto(compilation,
                        getEvents(compilation.getEvents(), events)))
                .toList();
    }

    //вспомогательные методы

    private static List<EventShortDto> getEvents(Collection<Long> ids, HashMap<Long, EventShortDto> eventShortDtoList) {
        return ids.stream().map(eventShortDtoList::get).toList();
    }
}
