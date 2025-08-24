package ru.yandex.practicum.collector.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.collector.configuration.UserActionProducerProperties;
import ru.yandex.practicum.grpc.stat.user_action.ActionTypeProto;
import ru.yandex.practicum.grpc.stat.user_action.UserActionProto;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserActionHandler {

    private final KafkaActionProducer producer;
    private final UserActionProducerProperties properties;

    public void handle(UserActionProto action) {

        UserActionAvro userActionAvro = UserActionAvro.newBuilder()
                .setUserId(action.getUserId())
                .setEventId(action.getEventId())
                .setActionType(toAvro(action.getActionType()))
                .setTimestamp(toInstant(action.getTimestamp()))
                .build();
        String topic = properties.getTopics().getFirst();
        producer.sendUserAction(topic, userActionAvro);
    }

    private static ActionTypeAvro toAvro(ActionTypeProto proto) {
        return switch (proto) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            case UNRECOGNIZED -> throw new IllegalArgumentException("Unknown ActionTypeProto: " + proto);
        };
    }

    private static Instant toInstant(com.google.protobuf.Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}
