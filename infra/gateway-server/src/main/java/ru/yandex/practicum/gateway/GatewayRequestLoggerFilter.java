package ru.yandex.practicum.gateway;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

@Component
@RequiredArgsConstructor
@Slf4j
public class GatewayRequestLoggerFilter implements GlobalFilter, Ordered {

    private final DiscoveryClient discoveryClient;

    @PostConstruct
    public void init() {
        log.info("[Gateway] >>> Фильтр GatewayRequestLoggerFilter инициализирован");
        List<String> services = discoveryClient.getServices();
        if (services.isEmpty()) {
            log.warn("[Gateway] >>> Список сервисов пуст");
        } else {
            log.info("[Gateway] >>> Найдено {} сервис(ов):", services.size());
            for (String serviceId : services) {
                List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
                for (ServiceInstance instance : instances) {
                    log.info(" - {}: {}:{}{}", serviceId,
                            instance.getHost(),
                            instance.getPort(),
                            instance.getUri().getPath());
                }
            }
        }
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("\nNew call of filter");
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getRawPath();

        log.info("[Gateway] Пришел запрос: {} {}", method, path);

        return chain.filter(exchange)
                .doFinally(aVoid -> {
                    Object targetUri = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
                    log.info("[Gateway] Отправлен запрос в сервис: {} {}", method,
                            targetUri != null ? targetUri : "(URI не определён)");
                });
    }

    @Override
    public int getOrder() {
        return -1; // чтобы фильтр был максимально ранним
    }
}