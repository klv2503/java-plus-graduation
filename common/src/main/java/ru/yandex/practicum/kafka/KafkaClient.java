package ru.yandex.practicum.kafka;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

import java.util.Map;

public interface KafkaClient {

    Producer<String, SpecificRecordBase> getProducer(Map<String, String> properties);

    Consumer<String, SpecificRecordBase> getConsumer(Map<String, String> properties);

    KafkaProducer<String, SpecificRecordBase> getKafkaProducer(Map<String, String> properties);

    KafkaConsumer<String, SpecificRecordBase> getKafkaConsumer(Map<String, String> properties);

    void stop();
}
