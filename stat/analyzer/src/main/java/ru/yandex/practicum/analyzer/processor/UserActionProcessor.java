package ru.yandex.practicum.analyzer.processor;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.analyzer.configuration.UserActionConsumerProperties;
import ru.yandex.practicum.analyzer.service.UserActionService;
import ru.yandex.practicum.kafka.KafkaClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionProcessor implements Runnable {
    private final KafkaClient client;
    private final UserActionConsumerProperties userActionConsumerProperties;
    private final UserActionService service;
    protected KafkaConsumer<String, SpecificRecordBase> consumer;

    private volatile boolean running = true;
    private Thread workerThread;

    @Override
    public void run() {
        consumer = client.getKafkaConsumer(userActionConsumerProperties.getProperties());
        consumer.subscribe(userActionConsumerProperties.getTopics());
        log.info("UserActionProcessor: Subscribed to topics: {}", consumer.subscription());
        try {
            while (true) {
                ConsumerRecords<String, SpecificRecordBase> records =
                        consumer.poll(userActionConsumerProperties.getPollTimeout());
                for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                    log.info("UserActionProcessor: records {}", records);
                    UserActionAvro action = (UserActionAvro) record.value();
                    service.addUserAction(action);
                }
            }
        } catch (WakeupException e) {
            log.info("UserActionProcessor stopping");
        } catch (Exception e) {
            log.error("Unexpected error in UserActionProcessor", e);
        } finally {
            consumer.close();
        }
    }

    public void start() {
        workerThread = new Thread(this, "UserActionProcessorThread");
        workerThread.start();
    }

    @PreDestroy
    public void shutdown() {
        log.info("UserActionProcessor shutdown triggered");
        running = false;
        if (consumer != null) {
            consumer.wakeup();
        }
        if (workerThread != null) {
            try {
                workerThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted while waiting for UserActionProcessor thread to finish");
            }
        }
    }
}
