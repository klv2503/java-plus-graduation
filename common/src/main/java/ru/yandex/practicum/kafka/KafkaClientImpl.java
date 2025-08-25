package ru.yandex.practicum.kafka;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Component
@RequiredArgsConstructor
public class KafkaClientImpl implements KafkaClient {

    private final List<Producer<String, SpecificRecordBase>> producers = new ArrayList<>();
    private final List<Consumer<String, SpecificRecordBase>> consumers = new ArrayList<>();
    private final List<KafkaProducer<String, SpecificRecordBase>> kafkaProducers = new ArrayList<>();
    private final List<KafkaConsumer<String, SpecificRecordBase>> kafkaConsumers = new ArrayList<>();

    @Override
    public Producer<String, SpecificRecordBase> getProducer(Map<String, String> properties) {
        Producer<String, SpecificRecordBase> producer = createProducer(properties);
        producers.add(producer);
        return producer;
    }

    @Override
    public Consumer<String, SpecificRecordBase> getConsumer(Map<String, String> properties) {
        Consumer<String, SpecificRecordBase> consumer = createConsumer(properties);
        consumers.add(consumer);
        return consumer;
    }

    @Override
    public KafkaProducer<String, SpecificRecordBase> getKafkaProducer(Map<String, String> properties) {
        KafkaProducer<String, SpecificRecordBase> producer = createKafkaProducer(properties);
        kafkaProducers.add(producer);
        return producer;
    }

    @Override
    public KafkaConsumer<String, SpecificRecordBase> getKafkaConsumer(Map<String, String> properties) {
        KafkaConsumer<String, SpecificRecordBase> consumer = createKafkaConsumer(properties);
        kafkaConsumers.add(consumer);
        return consumer;
    }

    private Producer<String, SpecificRecordBase> createProducer(Map<String, String> properties) {
        Properties props = new Properties();
        props.putAll(properties);
        return new KafkaProducer<>(props);
    }

    private Consumer<String, SpecificRecordBase> createConsumer(Map<String, String> properties) {
        Properties props = new Properties();
        props.putAll(properties);
        return new KafkaConsumer<>(props);
    }

    private KafkaProducer<String, SpecificRecordBase> createKafkaProducer(Map<String, String> properties) {
        Properties props = new Properties();
        props.putAll(properties);
        return new KafkaProducer<>(props);
    }

    private KafkaConsumer<String, SpecificRecordBase> createKafkaConsumer(Map<String, String> properties) {
        Properties props = new Properties();
        props.putAll(properties);
        return new KafkaConsumer<>(props);
    }

    @Override
    @PreDestroy
    public void stop() {
        producers.forEach(Producer::close);
        consumers.forEach(Consumer::close);
        kafkaProducers.forEach(Producer::close);
        kafkaConsumers.forEach(Consumer::close);

    }
}
