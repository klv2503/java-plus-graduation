package ru.yandex.practicum.aggregator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.aggregator.model.Interaction;
import ru.yandex.practicum.aggregator.model.InteractionId;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, InteractionId> {
}
