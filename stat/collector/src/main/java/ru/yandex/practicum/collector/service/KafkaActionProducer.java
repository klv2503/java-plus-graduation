package ru.yandex.practicum.collector.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.collector.configuration.UserActionProducerProperties;
import ru.yandex.practicum.kafka.KafkaClient;

@Service
@Getter
@RequiredArgsConstructor
@Slf4j
public class KafkaActionProducer {

    private final UserActionProducerProperties config;
    private final KafkaClient client;

    public void sendUserAction(String topic, SpecificRecordBase userAction) {
        log.trace("\nKafkaActionProducer: userAction {}", userAction);

        if (topic == null) {
            throw new IllegalArgumentException("Unknown topic: " + topic);
        }
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(topic, null, userAction);
        client.getProducer(config.getProperties()).send(record);
    }
}

