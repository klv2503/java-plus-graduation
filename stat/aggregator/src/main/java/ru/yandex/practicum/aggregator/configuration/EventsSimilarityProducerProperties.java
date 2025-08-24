package ru.yandex.practicum.aggregator.configuration;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
@Component
@ConfigurationProperties("aggregator.kafka.producer")
public class EventsSimilarityProducerProperties {
    private Map<String, String> properties;
    private List<String> topics;

    @PostConstruct
    public void logConfig() {
        System.out.println("EventsSimilarity Producer Properties: " + properties);
        System.out.println("EventsSimilarity Producer Topics: " + topics);
    }
}
