package ru.yandex.practicum.kafka.deserializer;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Component
public class EventSimilarityAvroDeserializer extends BaseAvroDeserializer<EventSimilarityAvro> {
    public EventSimilarityAvroDeserializer() {
        super(EventSimilarityAvro.getClassSchema());
    }
}
