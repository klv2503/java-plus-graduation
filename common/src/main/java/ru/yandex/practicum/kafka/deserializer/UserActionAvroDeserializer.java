package ru.yandex.practicum.kafka.deserializer;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Component
public class UserActionAvroDeserializer extends BaseAvroDeserializer<UserActionAvro> {
    public UserActionAvroDeserializer() {
        super(UserActionAvro.getClassSchema());
    }
}
