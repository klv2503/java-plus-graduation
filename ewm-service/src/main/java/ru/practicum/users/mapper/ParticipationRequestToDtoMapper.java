package ru.practicum.users.mapper;

import ru.practicum.users.dto.ParticipationRequestDto;
import ru.practicum.users.model.ParticipationRequest;

public class ParticipationRequestToDtoMapper {

    public static ParticipationRequestDto mapToDto(ParticipationRequest request) {
        ParticipationRequestDto dto = new ParticipationRequestDto();
        dto.setId(request.getId());
        dto.setCreated(request.getCreated());
        dto.setStatus(request.getStatus());
        dto.setRequester(request.getUser().getId());
        dto.setEvent(request.getEvent().getId());
        return dto;
    }

}
