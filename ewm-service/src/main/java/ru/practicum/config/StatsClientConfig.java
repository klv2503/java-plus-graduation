package ru.practicum.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class StatsClientConfig {
    @Value("${stats.client.host}")
    private String host;

    @Value("${stats.client.port}")
    private String port;
}
