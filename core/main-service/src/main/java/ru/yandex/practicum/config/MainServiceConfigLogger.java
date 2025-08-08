package ru.yandex.practicum.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MainServiceConfigLogger implements ApplicationListener<WebServerInitializedEvent> {
    private int actualPort;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        this.actualPort = event.getWebServer().getPort();
        log.info("✅ Application started on port: {}", actualPort);
    }

    @PostConstruct
    public void logDatabaseUrl() {
        log.info("✅ Connected to DB: {}", dbUrl);
    }

}