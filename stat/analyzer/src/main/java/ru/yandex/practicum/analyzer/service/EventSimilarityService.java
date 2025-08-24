package ru.yandex.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.yandex.practicum.analyzer.model.similarity.Similarity;
import ru.yandex.practicum.analyzer.model.similarity.SimilarityId;
import ru.yandex.practicum.analyzer.repository.SimilarityRepository;
import ru.yandex.practicum.grpc.stat.recommendation.RecommendedEventProto;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventSimilarityService {
    private final SimilarityRepository similarityRepository;
    private final UserActionService userActionService;

    @Transactional
    public void saveNewSimilarity(EventSimilarityAvro avro) {
        SimilarityId id = SimilarityId.builder()
                .eventA(avro.getEventA())
                .eventB(avro.getEventB())
                .build();

        Similarity similarity = Similarity.builder()
                .id(id)
                .score(avro.getScore())
                .timestamp(avro.getTimestamp())
                .build();

        similarityRepository.save(similarity);
        log.debug("Saved similarity: {}", similarity);
    }

    /**
     * Получить top-N похожих событий для указанного события.
     * Исключает события, с которыми пользователь уже взаимодействовал.
     */
    public List<RecommendedEventProto> getTopRecommendationsForUser(long userId, long eventId, int limit) {
        // исключаем события, с которыми пользователь уже взаимодействовал
        List<Long> excluded = userActionService.getUsersInteraction(userId).stream()
                .map(inter -> inter.getId().getEventId())
                .toList();

        // достаём все сходства для события
        List<Similarity> sims = similarityRepository.findByIdEventAOrIdEventB(eventId, eventId);

        return sims.stream()
                .filter(sim -> {
                    long otherEvent =
                            sim.getId().getEventA() == eventId ? sim.getId().getEventB() : sim.getId().getEventA();
                    return !excluded.contains(otherEvent);
                })
                .sorted(Comparator.comparingDouble(Similarity::getScore).reversed())
                .limit(limit)
                .map(sim -> {
                    long recommendedEventId =
                            sim.getId().getEventA() == eventId ? sim.getId().getEventB() : sim.getId().getEventA();
                    return RecommendedEventProto.newBuilder()
                            .setEventId(recommendedEventId)
                            .setScore(sim.getScore())
                            .build();
                })
                .toList();
    }
}
