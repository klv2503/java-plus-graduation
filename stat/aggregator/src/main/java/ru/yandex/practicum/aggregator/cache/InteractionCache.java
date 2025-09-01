package ru.yandex.practicum.aggregator.cache;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.aggregator.dto.InteractionDto;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
@Data
public class InteractionCache {
    // userId -> (eventId -> weight)
    private final Map<Long, Map<Long, Double>> userEventWeights = new HashMap<>();

    // eventId -> sum(weight)
    private final Map<Long, Double> eventSums = new HashMap<>();

    // Верхнетреугольная матрица: firstEvent < secondEvent -> S_min
    private final Map<Long, Map<Long, Double>> sMin = new HashMap<>();

    public double getEventSum(long eventId) {
        return eventSums.getOrDefault(eventId, 0.0);
    }

    public Map<Long, Double> getUserWeights(long userId) {
        return userEventWeights.getOrDefault(userId, Map.of());
    }

    public Map<Long, Double> getUpperEventSimilarity(long eventId) {
        return sMin.getOrDefault(eventId, Map.of());
    }

    public Map<Long, Double> getAllEventSimilarities(long eventId) {
    //Возвращает Map<Long, Double> где keySet это id всех event с ненулевым подобием с мероприятием с eventId
        // Сначала выбираем все пары, где eventId — первый в паре
        Map<Long, Double> upper = sMin.getOrDefault(eventId, Map.of());
        Map<Long, Double> result = new HashMap<>(upper);

        // Теперь все пары, где eventId — второй в паре
        for (Map.Entry<Long, Map<Long, Double>> entry : sMin.entrySet()) {
            long otherEvent = entry.getKey();
            if (otherEvent >= eventId) continue; // берём только пары с другим событием < eventId
            Double value = entry.getValue().get(eventId);
            if (value != null) {
                result.put(otherEvent, value);
            }
        }

        return result;
    }

    public boolean addInteraction(InteractionDto interaction) {
        boolean wasChanged = false;
        long userId = interaction.getUserId();
        long eventId = interaction.getEventId();
        double newWeight = interaction.getWeight();

        Map<Long, Double> eventMap = userEventWeights.computeIfAbsent(userId, k -> new HashMap<>());
        double oldWeight = eventMap.getOrDefault(eventId, 0.0);

        // Пересчет, если новый вес больше
        if (Double.compare(newWeight, oldWeight) > 0) {
            var diff = newWeight - oldWeight;
            eventMap.put(eventId, newWeight);
            eventSums.merge(eventId, diff, Double::sum);

            // обновляем sMin для всех событий этого юзера
            eventMap.entrySet().stream()
                    .filter(e -> e.getKey() != eventId)
                    .map(e -> {
                        var prevMin = Math.min(oldWeight, e.getValue());
                        var newMin = Math.min(newWeight, e.getValue());
                        var delta = newMin - prevMin;
                        return Map.entry(e.getKey(), delta);
                    })
                    .filter(entry -> entry.getValue() > 0)
                    .forEach(e -> updateSMin(eventId, e.getKey(), e.getValue()));
            wasChanged = true;
        }
        return wasChanged;
    }

    private void updateSMin(long eventId1, long eventId2, double delta) {
        var first = Math.min(eventId1, eventId2);
        var second = Math.max(eventId1, eventId2);
        sMin.computeIfAbsent(first, k -> new HashMap<>())
                .merge(second, delta, Double::sum);
    }

    public double getSMinBetween(long a, long b) {
        if (a == b) return 0.0;
        var first = Math.min(a, b);
        var second = Math.max(a, b);
        return sMin.getOrDefault(first, Map.of()).getOrDefault(second, 0.0);
    }

}