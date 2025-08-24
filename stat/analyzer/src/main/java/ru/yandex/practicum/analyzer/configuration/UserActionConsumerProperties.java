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
@ConfigurationProperties("analyzer.kafka.consumers.action")
public class UserActionConsumerProperties {
    private Map<String, String> properties;
    private List<String> topics;
    private Duration pollTimeout;

    @PostConstruct
    public void logConfig() {
        System.out.println("Analyzer.UserAction Consumer Properties: " + properties);
        System.out.println("Analyzer.UserAction Consumer Topics: " + topics);
        System.out.println("Analyzer.UserAction Consumer poll-timeout: " + pollTimeout);

    }

}
