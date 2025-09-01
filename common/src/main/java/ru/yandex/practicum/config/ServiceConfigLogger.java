package ru.yandex.practicum.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ServiceConfigLogger implements ApplicationListener<WebServerInitializedEvent> {

    @Value("${spring.application.name:Application}")
    private String serviceName;

    @Value("${spring.datasource.url:unknown}")
    private String dbUrl;

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        int actualPort = event.getWebServer().getPort();
        log.info("✅ {} started on port: {}", serviceName, actualPort);
        log.info("✅ {} connected to DB: {}", serviceName, dbUrl);
    }

}