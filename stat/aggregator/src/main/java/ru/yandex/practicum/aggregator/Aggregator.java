package ru.yandex.practicum.aggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import ru.yandex.practicum.aggregator.aggregation.AggregationStarter;

@SpringBootApplication(
        scanBasePackages = {"ru.yandex.practicum.aggregator", "ru.yandex.practicum.enums", "ru.yandex.practicum.kafka"}
)
@ConfigurationPropertiesScan
public class Aggregator {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Aggregator.class, args);

        // Получаем бин AggregationStarter из контекста и запускаем основную логику сервиса
        AggregationStarter aggregator = context.getBean(AggregationStarter.class);
        try {
            aggregator.start();
        } finally {
            SpringApplication.exit(context);
        }
    }
}
