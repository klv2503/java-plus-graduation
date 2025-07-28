package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.CreateEndpointHitDto;
import ru.practicum.dto.ManyEndPointDto;
import ru.practicum.dto.ReadEndpointHitDto;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;

@RequiredArgsConstructor
@Slf4j
public class ClientController {

    String hostName;
    String port;

    private final RestClient restClient;

    public ClientController(String hostName, String port) {
        this.hostName = hostName;
        this.port = port;
        this.restClient = RestClient.create();
    }

    public ResponseEntity<Void> saveView(String addr, String uri) {
        log.info("\nClientController.saveView addr {}, uri {}", addr, uri);
        CreateEndpointHitDto dto = new CreateEndpointHitDto(
                "ewm-main-service",
                uri,
                addr,
                LocalDateTime.now()
        );

        restClient.post()
                .uri(buildUri("/hit", Map.of()))
                .body(dto)
                .retrieve()
                .toBodilessEntity();

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    public List<ReadEndpointHitDto> getHits(String start, String end, List<String> uris, boolean unique) {
        log.info("\nClientController.getHits start {}, end {}, \nuris {}, unique {}", start, end, uris, unique);

        Map<String, String> params = new HashMap<>();
        params.put("start", start);
        params.put("end", end);
        params.put("uris", String.join(",", uris));
        params.put("unique", String.valueOf(unique));
        // Выполняем запрос и получаем коллекцию объектов ReadEndpointHitDto
        ResponseEntity<Collection<ReadEndpointHitDto>> response = restClient.get()
                .uri(buildUri("/stats", params))
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {
                });

        List<ReadEndpointHitDto> respList = Optional.ofNullable(response.getBody())
                .map(ArrayList::new)
                .orElseGet(ArrayList::new);
        return respList;
    }

    public ResponseEntity<Void> saveHitsGroup(List<String> uris, String ip) {
        log.info("\nClientController.saveHitsGroup uris {}, addr {}", uris, ip);

        ManyEndPointDto manyEndPointDto = ManyEndPointDto.builder()
                .uris(uris)
                .ip(ip)
                .build();

        log.info("\nClientController.saveHitsGroup many {}", manyEndPointDto);

        restClient.post()
                .uri(buildUri("/hit/group", Map.of()))
                .body(manyEndPointDto)
                .retrieve()
                .toBodilessEntity();
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private URI buildUri(String path, Map<String, String> queryParams) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(hostName)
                .port(port)
                .path(path);

        // Добавляем параметры
        queryParams.forEach(uriComponentsBuilder::queryParam);

        return uriComponentsBuilder.build().toUri();
    }

}



