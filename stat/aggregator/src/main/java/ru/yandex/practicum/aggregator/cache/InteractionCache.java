package ru.yandex.practicum.aggregator.cache;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.aggregator.model.Interaction;
import ru.yandex.practicum.aggregator.model.Similarity;
import ru.yandex.practicum.aggregator.repository.EventSumsRepository;
import ru.yandex.practicum.aggregator.repository.SimilarityRepository;
import ru.yandex.practicum.aggregator.repository.InteractionRepository;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
@Data
public class InteractionCache {

    private final InteractionRepository interactionRepository;

    private final SimilarityRepository similarityRepository;

    private final EventSumsRepository eventSumsRepository;

    // userId -> (eventId -> weight)
    private final Map<Long, Map<Long, Double>> userEventWeights = new HashMap<>();

    // eventId -> sum(weight)
    private final Map<Long, Double> eventSums = new HashMap<>();

    // Верхнетреугольная матрица: firstEvent < secondEvent -> S_min
    private final Map<Long, Map<Long, Double>> sMin = new HashMap<>();


    @PostConstruct
    public void init() {
        log.info("Initializing InteractionCache...");
        List<Interaction> all = interactionRepository.findAll();

        for (Interaction interaction : all) {
            long userId = interaction.getId().getUserId();
            long eventId = interaction.getId().getEventId();
            double weight = interaction.getWeight();

            Map<Long, Double> userWeights = userEventWeights
                    .computeIfAbsent(userId, k -> new HashMap<>());
            userWeights.put(eventId, weight);
        }
        eventSumsRepository.findAll().forEach(sum ->
                eventSums.put(sum.getEventId(), sum.getScore())
        );

        List<Similarity> similarities = similarityRepository.findAll();
        for (Similarity similarity : similarities) {
            long first = similarity.getId().getEventA();
            long second = similarity.getId().getEventB();
            double score = similarity.getScore();

            sMin.computeIfAbsent(first, k -> new HashMap<>())
                    .put(second, score);
        }
        log.info("Loaded {} users, {} event sums and {} similarities into cache",
                userEventWeights.size(), eventSums.size(), similarities.size());
    }

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

    public boolean addInteraction(Interaction interaction) {
        long userId = interaction.getId().getUserId();
        long eventId = interaction.getId().getEventId();
        double newWeight = interaction.getWeight();

        Map<Long, Double> eventMap = userEventWeights.computeIfAbsent(userId, k -> new HashMap<>());
        Double oldWeight = eventMap.get(eventId);

        // Первое действие юзера
        if (oldWeight == null) {
            eventMap.put(eventId, newWeight);
            eventSums.merge(eventId, newWeight, Double::sum);

            // обновляем sMin для всех существующих событий этого юзера
            for (Map.Entry<Long, Double> entry : eventMap.entrySet()) {
                long otherEvent = entry.getKey();
                if (otherEvent == eventId) continue;
                long first = Math.min(eventId, otherEvent);
                long second = Math.max(eventId, otherEvent);
                sMin.computeIfAbsent(first, k -> new HashMap<>())
                        .merge(second, Math.min(newWeight, entry.getValue()), Double::sum);
            }
            return true;
        }

        // Пересчет, если новый вес больше
        if (newWeight > oldWeight) {
            double diff = newWeight - oldWeight;
            eventMap.put(eventId, newWeight);
            eventSums.merge(eventId, diff, Double::sum);

            // обновляем sMin для всех событий этого юзера
            for (Map.Entry<Long, Double> entry : eventMap.entrySet()) {
                long otherEvent = entry.getKey();
                if (otherEvent == eventId) continue;

                long first = Math.min(eventId, otherEvent);
                long second = Math.max(eventId, otherEvent);

                double prevMin = Math.min(oldWeight, entry.getValue());
                double newMin = Math.min(newWeight, entry.getValue());
                double delta = newMin - prevMin;

                if (delta > 0) {
                    sMin.computeIfAbsent(first, k -> new HashMap<>())
                            .merge(second, delta, Double::sum);
                }
            }
            return true;
        }
        return false;
    }

    public double getSMinBetween(long a, long b) {
        if (a == b) return 0.0;
        long first = Math.min(a, b);
        long second = Math.max(a, b);
        return sMin.getOrDefault(first, Map.of()).getOrDefault(second, 0.0);
    }


}