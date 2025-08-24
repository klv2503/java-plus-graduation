package ru.yandex.practicum.analyzer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import ru.yandex.practicum.analyzer.processor.EventSimilarityProcessor;
import ru.yandex.practicum.analyzer.processor.UserActionProcessor;

@SpringBootApplication(
        scanBasePackages = {"ru.yandex.practicum.analyzer", "ru.yandex.practicum.enums", "ru.yandex.practicum.kafka"}
)
@ConfigurationPropertiesScan
@Slf4j
public class Analyzer {
    public static void main(String[] args) {
        ConfigurableApplicationContext context =
                SpringApplication.run(Analyzer.class, args);

        final EventSimilarityProcessor eventSimilarityProcessor =
                context.getBean(EventSimilarityProcessor.class);
        log.info("EventSimilarityProcessor loaded: {}", eventSimilarityProcessor);
        UserActionProcessor userActionProcessor =
                context.getBean(UserActionProcessor.class);
        log.info("UserActionProcessor loaded: {}", userActionProcessor);

        // запускаем обработчик событий от агрегатора
        eventSimilarityProcessor.start();

        // начинаем обработку действий пользователя
        userActionProcessor.start();
    }
}