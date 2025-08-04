package ru.yandex.practicum.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

@Component
public class GatewayRequestLoggerFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(GatewayRequestLoggerFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getRawPath();

        log.info("[Gateway] Пришел запрос: {} {}", method, path);

        return chain.filter(exchange)
                .doOnSuccess(aVoid -> {
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