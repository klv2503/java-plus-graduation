package ru.yandex.practicum.aggregator.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "interactions")
public class Interaction {

    @EmbeddedId
    private InteractionId id;

    @Column(name = "weight")
    private double weight;

}
