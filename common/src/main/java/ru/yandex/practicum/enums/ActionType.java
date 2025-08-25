package ru.yandex.practicum.enums;

import lombok.Getter;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;

@Getter
public enum ActionType {
    VIEW(0.4),
    REGISTER(0.8),
    LIKE(1.0);

    private final double weight;

    ActionType(double weight) {
        this.weight = weight;
    }

    public static ActionType fromAvro(ActionTypeAvro avro) {
        return switch (avro) {
            case VIEW -> VIEW;
            case REGISTER -> REGISTER;
            case LIKE -> LIKE;
        };
    }

}
