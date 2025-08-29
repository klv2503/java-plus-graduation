package ru.yandex.practicum.collector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(
        scanBasePackages = {"ru.yandex.practicum.collector", "ru.yandex.practicum.enums", "ru.yandex.practicum.kafka"}
)
@ConfigurationPropertiesScan
public class Collector {
    public static void main(String[] args) {
        SpringApplication.run(Collector.class, args);
    }
}