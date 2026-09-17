package ru.yanin.fraud_detector.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.yanin.fraud_detector.service.neo4j.TransactionExistenceChecker;
import ru.yanin.fraud_detector.service.pipeline.Pipeline;
import ru.yanin.shared.domain.TransactionEvent;
import ru.yanin.shared.producer.Producer;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionConsumer {

    private final Pipeline pipeline;
    private final TransactionExistenceChecker transactionExistenceChecker;
    private final Producer<TransactionEvent> manualReviewProducer;

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${app.kafka.topics.raw-transactions}",
            batch = "false"
    )
    public void consume(TransactionEvent event, Acknowledgment ack) {
        log.info("Consume event {}", event);
        try {
            boolean isAlreadySaved = transactionExistenceChecker.isTransactionAlreadySavedWithRetry(event);
            if (!isAlreadySaved) {
                manualReviewProducer.sendMessage(event);
                return;
            }
            pipeline.flow(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed event with id {} cause: {}", event.transactionId(), e.getMessage(), e);
        }
    }
}
