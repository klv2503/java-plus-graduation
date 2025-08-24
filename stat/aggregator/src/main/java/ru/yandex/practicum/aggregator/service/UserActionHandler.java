package ru.yandex.practicum.aggregator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.aggregator.cache.InteractionCache;
import ru.yandex.practicum.aggregator.model.Interaction;
import ru.yandex.practicum.aggregator.model.InteractionId;
import ru.yandex.practicum.enums.ActionType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserActionHandler {

    private final InteractionCache cache;

    public List<EventSimilarityAvro> userActionHandle(UserActionAvro userActionAvro) {

        Instant requestTime = userActionAvro.getTimestamp();
        Interaction newInteraction = Interaction.builder()
                .id(InteractionId.builder()
                        .userId(userActionAvro.getUserId())
                        .eventId(userActionAvro.getEventId())
                        .build())
                .weight(ActionType.fromAvro(userActionAvro.getActionType()).getWeight())
                .build();
        return cache.addInteraction(newInteraction) ?
                computeSimilarities(newInteraction.getId().getEventId(),
                        requestTime,
                        newInteraction.getId().getUserId())
                : List.of();
    }

    private List<EventSimilarityAvro> computeSimilarities(long eventId, Instant timestamp, long userId) {
        List<EventSimilarityAvro> result = new ArrayList<>();

        double sA = cache.getEventSum(eventId);
        if (sA <= 0.0) {
            return result; // нет взаимодействий — нечего считать
        }

        // Берём все события, с которыми взаимодействовал пользователь
        Map<Long, Double> userWeights = cache.getUserWeights(userId);
        if (userWeights == null || userWeights.isEmpty()) {
            return result;
        }

        for (Long otherEventId : userWeights.keySet()) {
            if (otherEventId == eventId) {
                continue; // пропускаем совпадение с самим собой
            }

            // Достаём sMin без учёта ориентации
            double sMin = cache.getSMinBetween(eventId, otherEventId);
            if (sMin <= 0.0) {
                continue; // нет общей связи
            }

            double sB = cache.getEventSum(otherEventId);
            if (sB <= 0.0) {
                continue; // событие пустое
            }

            double similarity = sMin / Math.sqrt(sA * sB);

            long eventA = Math.min(eventId, otherEventId);
            long eventB = Math.max(eventId, otherEventId);

            EventSimilarityAvro similarityAvro = EventSimilarityAvro.newBuilder()
                    .setEventA(eventA)
                    .setEventB(eventB)
                    .setScore(similarity)
                    .setTimestamp(timestamp)
                    .build();

            result.add(similarityAvro);
        }

        // сортируем по убыванию score
        result.sort((e1, e2) -> Double.compare(e2.getScore(), e1.getScore()));

        return result;
    }
}
