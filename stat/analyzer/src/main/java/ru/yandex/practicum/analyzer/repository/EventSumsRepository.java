package ru.yandex.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.analyzer.model.EventSums;

public interface EventSumsRepository extends JpaRepository<EventSums, Long> {
}
