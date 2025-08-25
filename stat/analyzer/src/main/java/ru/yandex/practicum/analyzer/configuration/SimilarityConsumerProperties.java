package ru.yandex.practicum.analyzer.configuration;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
@Component
@ConfigurationProperties("analyzer.kafka.consumers.similarity")

public class SimilarityConsumerProperties {
    private Map<String, String> properties;
    private List<String> topics;
    private Duration pollTimeout;

    @PostConstruct
    public void logConfig() {
        System.out.println("Similarity Consumer Properties: " + properties);
        System.out.println("Similarity Consumer Topics: " + topics);
        System.out.println("Similarity Consumer poll-timeout: " + pollTimeout);
    }
}