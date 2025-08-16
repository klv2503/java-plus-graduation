package ru.yandex.practicum.mapper;


import ru.yandex.practicum.dto.request.ParticipationRequestDto;
import ru.yandex.practicum.model.ParticipationRequest;

public class ParticipationRequestToDtoMapper {

    public static ParticipationRequestDto mapToDto(ParticipationRequest request) {
        ParticipationRequestDto dto = new ParticipationRequestDto();
        dto.setId(request.getId());
        dto.setCreated(request.getCreated());
        dto.setStatus(request.getStatus());
        dto.setRequester(request.getUserId());
        dto.setEvent(request.getEvent());
        return dto;
    }

}
