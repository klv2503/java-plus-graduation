package ru.yandex.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.analyzer.model.interaction.Interaction;
import ru.yandex.practicum.analyzer.model.interaction.InteractionId;

import java.util.List;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, InteractionId> {

    List<Interaction> findByIdUserId(long userId);

    List<Interaction> findTop10ByIdUserIdOrderByIdEventIdDesc(long userId);

    @Query("""
        SELECT COALESCE(SUM(i.weight), 0)
        FROM Interaction i
        WHERE i.id.eventId = :eventId
        """)
    double sumWeightsByEventId(@Param("eventId") long eventId);
}
