package ru.yandex.practicum;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Component
@Slf4j
public class EventServiceConfigLogger implements ApplicationListener<WebServerInitializedEvent> {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    private final RequestMappingHandlerMapping mapping;

    public EventServiceConfigLogger(@Qualifier("requestMappingHandlerMapping")
                                    RequestMappingHandlerMapping mapping) {
        this.mapping = mapping;
    }
    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        int actualPort = event.getWebServer().getPort();
        log.info("✅ Application started on port: {}", actualPort);
    }

    @PostConstruct
    public void logDatabaseUrl() {
        log.info("✅ Connected to DB: {}", dbUrl);
    }

    @PostConstruct
    public void printMappings() {
        log.info("=== All request mappings ===");
        mapping.getHandlerMethods().forEach((key, value) ->
                log.info("{} -> {}", key, value));
    }
}