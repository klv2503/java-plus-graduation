package ru.yandex.practicum.aggregator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.aggregator.model.Similarity;
import ru.yandex.practicum.aggregator.model.SimilarityId;

public interface SimilarityRepository extends JpaRepository<Similarity, SimilarityId> {
}