package ru.yandex.practicum.gateway;

import org.springframework.boot.actuate.health.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayHealthConfig {

    @Bean
    public ReactiveHealthIndicator mainServiceReadinessIndicator(WebClient.Builder builder) {
        WebClient client = builder.build();
        return () -> client.get()
                .uri("http://main-service/actuator/health")
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> Health.up().withDetail("main-service", "reachable").build())
                .onErrorResume(ex -> Mono.just(
                        Health.down()
                                .withDetail("main-service", "unreachable")
                                .withException(ex)
                                .build()
                ));
    }
}
