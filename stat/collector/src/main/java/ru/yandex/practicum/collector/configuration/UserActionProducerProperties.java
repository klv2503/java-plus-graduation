package ru.yandex.practicum.collector.configuration;

import jakarta.annotation.PostConstruct;
import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
@NoArgsConstructor
@ConfigurationProperties("collector.kafka.producer")
public class UserActionProducerProperties {
    private Map<String, String> properties;
    private List<String> topics;

    @PostConstruct
    public void logConfig() {
        System.out.println("Collector Producer Properties: " + properties);
        System.out.println("Collector Producer Topics: " + topics);
    }

}