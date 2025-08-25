package ru.yandex.practicum.aggregator.aggregation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.aggregator.configuration.EventsSimilarityProducerProperties;
import ru.yandex.practicum.aggregator.configuration.UserActionConsumerProperties;
import ru.yandex.practicum.aggregator.service.UserActionHandler;
import ru.yandex.practicum.kafka.KafkaClient;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private final KafkaClient client;
    private final EventsSimilarityProducerProperties producerProperties;
    private final UserActionConsumerProperties consumerProperties;
    protected KafkaConsumer<String, SpecificRecordBase> consumer;
    protected KafkaProducer<String, SpecificRecordBase> producer;
    private final UserActionHandler service;

    public void start() {
        consumer = client.getKafkaConsumer(consumerProperties.getProperties());
        consumer.subscribe(consumerProperties.getTopics());
        producer = client.getKafkaProducer(producerProperties.getProperties());

        try {
            while (true) {
                try {
                    ConsumerRecords<String, SpecificRecordBase> records =
                            consumer.poll(consumerProperties.getPollTimeout());
                    if (!records.isEmpty()) {
                        for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                            // Здесь происходит обработка полученных данных
                            UserActionAvro userActionAvro = (UserActionAvro) record.value();
                            log.trace("\nAggregationStarter: accepted {}", userActionAvro);
                            List<EventSimilarityAvro> similarities = service.userActionHandle(userActionAvro);

                            // Отправляем все элементы списка в Kafka
                            for (EventSimilarityAvro eventSimilarity : similarities) {
                                log.trace("AggregationStarter: sending eventSimilarity {}", eventSimilarity);
                                producer.send(new ProducerRecord<>(producerProperties.getTopics().getFirst(),
                                        null, eventSimilarity));
                            }
                        }
                        consumer.commitSync();
                    }
                } catch (WakeupException e) {
                    // Нормальный выход из цикла при остановке
                    log.info("Консьюмер был остановлен.");
                    break;
                } catch (Exception e) {
                    log.error("Ошибка при обработке данных", e);
                }
            }

        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмера и продюсера в блоке finally
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий", e);
        } finally {

            try {
                producer.flush();
                consumer.commitSync();
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }
}