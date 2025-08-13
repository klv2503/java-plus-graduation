package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.CreateEndpointHitDto;
import ru.yandex.practicum.dto.ManyEndPointDto;
import ru.yandex.practicum.dto.endpoint.ReadEndpointHitDto;
import ru.yandex.practicum.dto.TakeHitsDto;
import ru.yandex.practicum.model.EndpointHit;
import ru.yandex.practicum.repository.EndpointHitRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EndpointHitService {

    private final EndpointHitRepository endpointHitRepository;

    @Transactional
    public void saveHit(CreateEndpointHitDto endpointHitDto) {
        EndpointHit endpointHit = new EndpointHit();
        endpointHit.setApp(endpointHitDto.getApp());
        endpointHit.setUri(endpointHitDto.getUri());
        endpointHit.setIp(endpointHitDto.getIp());
        endpointHit.setTimestamp(endpointHitDto.getTimestamp() != null
                ? endpointHitDto.getTimestamp()
                : LocalDateTime.now());

        endpointHitRepository.save(endpointHit);
    }

    public Collection<ReadEndpointHitDto> getHits(TakeHitsDto takeHitsDto) {
        if (takeHitsDto.getEnd().isBefore(takeHitsDto.getStart())) {
            throw new IllegalArgumentException("Request dates are incorrect.", null);
        }

        Collection<ReadEndpointHitDto> hits = endpointHitRepository.get(takeHitsDto).stream()
                .sorted(Comparator.comparingInt(ReadEndpointHitDto::getHits)).toList().reversed();

        return hits;
    }

    @Transactional
    public void saveHitsGroup(ManyEndPointDto many) {
        //подготовка списка
        String app = "ewm-service";
        LocalDateTime nun = LocalDateTime.now();
        List<EndpointHit> hitsList = many.getUris().stream()
                .map(u -> EndpointHit.builder()
                        .app(app)
                        .uri(u)
                        .ip(many.getIp())
                        .timestamp(nun)
                        .build())
                .toList();
        endpointHitRepository.saveAll(hitsList);
    }
}
