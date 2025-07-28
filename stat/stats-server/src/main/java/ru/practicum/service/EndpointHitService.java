package ru.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CreateEndpointHitDto;
import ru.practicum.dto.ManyEndPointDto;
import ru.practicum.dto.ReadEndpointHitDto;
import ru.practicum.dto.TakeHitsDto;
import ru.practicum.model.EndpointHit;
import ru.practicum.repository.EndpointHitRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class EndpointHitService {

    private final EndpointHitRepository endpointHitRepository;

    @Autowired
    public EndpointHitService(EndpointHitRepository endpointHitRepository) {
        this.endpointHitRepository = endpointHitRepository;
    }

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
