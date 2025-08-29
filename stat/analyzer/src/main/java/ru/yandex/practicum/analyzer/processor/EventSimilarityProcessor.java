package ru.yandex.practicum.analyzer.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.yandex.practicum.analyzer.configuration.SimilarityConsumerProperties;
import ru.yandex.practicum.analyzer.service.EventSimilarityService;
import ru.yandex.practicum.kafka.KafkaClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSimilarityProcessor implements Runnable {
    private final KafkaClient client;
    private final SimilarityConsumerProperties similarityConsumerProperties;
    private final EventSimilarityService service;
    protected KafkaConsumer<String, SpecificRecordBase> consumer;

    private Thread workerThread;

    @Override
    public void run() {
        consumer = client.getKafkaConsumer(similarityConsumerProperties.getProperties());
        consumer.subscribe(similarityConsumerProperties.getTopics());
        log.info("EventSimilarityProcessor: Subscribed to topic: {}", consumer.subscription());
        try {
            while (true) {
                ConsumerRecords<String, SpecificRecordBase> records =
                        consumer.poll(similarityConsumerProperties.getPollTimeout());
                    for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                        log.info("\nEventSimilarityProcessor: accepted " + records);
                        EventSimilarityAvro event = (EventSimilarityAvro) record.value();
                        service.saveNewSimilarity(event);
                    }
            }
        } catch (WakeupException e) {
            log.info("Consumer wakeup called, shutting down");
        } catch (Exception e) {
            log.error("Unexpected error in EventSimilarityProcessor", e);
        } finally {
            consumer.close();
        }
    }

    public void start() {
        workerThread = new Thread(this, "EventSimilarityProcessorThread");
        workerThread.start();
    }

}