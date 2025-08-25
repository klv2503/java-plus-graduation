package ru.yandex.practicum.analyzer.model.similarity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimilarityId implements Serializable {
    @Column(name = "event_a")
    private Long eventA;

    @Column(name = "event_b")
    private Long eventB;
}