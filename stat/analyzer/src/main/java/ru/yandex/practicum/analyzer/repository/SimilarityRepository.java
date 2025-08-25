package ru.yandex.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.analyzer.model.similarity.Similarity;
import ru.yandex.practicum.analyzer.model.similarity.SimilarityId;

import java.util.List;

@Repository
public interface SimilarityRepository extends JpaRepository<Similarity, SimilarityId> {

    List<Similarity> findByIdEventAOrIdEventB(long eventA, long eventB);

    @Query("""
        SELECT s FROM Similarity s
        WHERE (s.id.eventA = :eventId OR s.id.eventB = :eventId)
          AND (CASE WHEN s.id.eventA = :eventId THEN s.id.eventB ELSE s.id.eventA END) NOT IN :excluded
        ORDER BY s.score DESC
        """)
    List<Similarity> findTopByEvent(
            @Param("eventId") long eventId,
            @Param("excluded") List<Long> excluded
    );
}