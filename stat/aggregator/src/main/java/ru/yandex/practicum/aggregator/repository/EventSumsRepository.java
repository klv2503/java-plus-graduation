package ru.yandex.practicum.aggregator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.aggregator.model.EventSums;

public interface EventSumsRepository extends JpaRepository<EventSums, Long> {
}
