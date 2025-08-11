package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.yandex.practicum.dto.CreateEndpointHitDto;
import ru.yandex.practicum.dto.ManyEndPointDto;
import ru.yandex.practicum.dto.ReadEndpointHitDto;
import ru.yandex.practicum.utils.ServiceUriResolver;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClientController {

    private final ServiceUriResolver serviceUriResolver;

    private static final String SERVICE_NAME = "stats-server";

    public ResponseEntity<Void> saveView(String addr, String uri) {
        log.info("\nClientController.saveView addr {}, uri {}", addr, uri);
        CreateEndpointHitDto dto = new CreateEndpointHitDto(
                "ewm-main-service",
                uri,
                addr,
                LocalDateTime.now()
        );

        URI fullUri = buildUri("/hit", Map.of());
        RestClient restClient = RestClient.create();
        restClient.post()
                .uri(fullUri)
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
        URI fullUri = buildUri("/stats", params);
        RestClient restClient = RestClient.create();
        ResponseEntity<Collection<ReadEndpointHitDto>> response = restClient.get()
                .uri(fullUri)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {
                });

        return Optional.ofNullable(response.getBody())
                .map(ArrayList::new)
                .orElseGet(ArrayList::new);
    }

    public ResponseEntity<Void> saveHitsGroup(List<String> uris, String ip) {
        log.info("\nClientController.saveHitsGroup uris {}, addr {}", uris, ip);

        ManyEndPointDto manyEndPointDto = ManyEndPointDto.builder()
                .uris(uris)
                .ip(ip)
                .build();

        log.info("\nClientController.saveHitsGroup many {}", manyEndPointDto);

        URI fullUri = buildUri("/hit/group", Map.of());
        RestClient restClient = RestClient.create();
        restClient.post()
                .uri(fullUri)
                .body(manyEndPointDto)
                .retrieve()
                .toBodilessEntity();
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private URI buildUri(String path, Map<String, String> queryParams) {
        URI baseUri = serviceUriResolver.getBaseUri(SERVICE_NAME);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(baseUri.getHost())
                .port(baseUri.getPort())
                .path(path);

        queryParams.forEach(uriBuilder::queryParam);

        URI fullUri = uriBuilder.build().toUri();
        log.info("\nResolved full URI for stats-service: {}", fullUri);
        return fullUri;
    }
}