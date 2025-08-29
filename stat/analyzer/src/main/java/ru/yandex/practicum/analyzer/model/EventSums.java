package ru.yandex.practicum.analyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sum_weights")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventSums {
    @Id
    @Column(name = "event_id")
    private long eventId;

    @Column(name = "score")
    private double score;
}
