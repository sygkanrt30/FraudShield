package ru.yanin.ingress_service.service.event;

import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yanin.ingress_service.metrcis.IngressMetrics;
import ru.yanin.ingress_service.model.dto.event.TransactionEventWithTimer;
import ru.yanin.ingress_service.model.entity.Status;
import ru.yanin.ingress_service.service.transaction_record.TransactionRecordService;
import ru.yanin.shared.domain.TransactionEvent;
import ru.yanin.shared.producer.Producer;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Service
public class EventKafkaProducer implements Producer<TransactionEventWithTimer> {

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;
    private final IngressMetrics metrics;
    private final TransactionRecordService transactionRecordService;
    private final String rawTransactionsTopic;

    public EventKafkaProducer(KafkaTemplate<String, TransactionEvent> kafkaTemplate,
                              IngressMetrics metrics,
                              TransactionRecordService transactionRecordService,
                              @Value("${app.kafka.topics.raw-transactions}") String rawTransactionsTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.metrics = metrics;
        this.transactionRecordService = transactionRecordService;
        this.rawTransactionsTopic = rawTransactionsTopic;
    }

    @Override
    public void sendMessage(TransactionEventWithTimer event) {
        kafkaTemplate.send(rawTransactionsTopic, event.transactionEvent())
                .whenComplete((result, throwable) -> {

                    UUID transactionId = result.getProducerRecord().value().transactionId();

                    if (Objects.isNull(throwable)) {
                        transactionRecordService.updateStatus(transactionId, Status.SENT_TO_KAFKA);
                        completingKafkaPublishMetricsRecord(true, event.kafkaTimer());
                        log.info("Transaction {} sent to Kafka", transactionId);
                    } else {
                        transactionRecordService.updateStatus(transactionId, Status.KAFKA_ERROR);
                        completingKafkaPublishMetricsRecord(false, event.kafkaTimer());
                        log.error("Kafka failed for {}", transactionId, throwable);
                    }
                });
    }

    private void completingKafkaPublishMetricsRecord(boolean success, Timer.Sample kafkaTimer) {
        metrics.incrementKafkaPublishCounter(success);
        metrics.stopKafkaTimer(kafkaTimer);
    }
}
