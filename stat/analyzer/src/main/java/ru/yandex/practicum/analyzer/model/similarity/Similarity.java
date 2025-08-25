package ru.yandex.practicum.analyzer.model.similarity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "similarities")
public class Similarity {

    @EmbeddedId
    private SimilarityId id;

    @Column(name = "score")
    private double score;

    @Column(name = "timestamp_ms")
    private Instant timestamp;
}