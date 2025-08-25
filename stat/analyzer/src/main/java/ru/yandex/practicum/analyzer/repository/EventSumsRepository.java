package ru.yandex.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.analyzer.model.EventSums;

@Repository
public interface EventSumsRepository extends JpaRepository<EventSums, Long> {
}
