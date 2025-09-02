package ru.yandex.practicum.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Component
@Slf4j
public class ServiceConfigLogger implements ApplicationListener<WebServerInitializedEvent> {

    @Value("${spring.application.name:Application}")
    private String serviceName;

    @Value("${spring.datasource.url:unknown}")
    private String dbUrl;

    private final RequestMappingHandlerMapping mapping;

    public ServiceConfigLogger(@Qualifier("requestMappingHandlerMapping")
                                    RequestMappingHandlerMapping mapping) {
        this.mapping = mapping;
    }

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        int actualPort = event.getWebServer().getPort();
        log.info("✅ {} started on port: {}", serviceName, actualPort);
        log.info("✅ {} connected to DB: {}", serviceName, dbUrl);
        log.info("=== All request mappings ===");
        mapping.getHandlerMethods().forEach((key, value) -> {
            if (!value.getBeanType().getName().startsWith("org.springframework.boot")) {
                log.info("{} -> {}", key, value);
            }
        });
    }

}
