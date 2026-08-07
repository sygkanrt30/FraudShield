package ru.yanin.fraud_detector.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.yanin.fraud_detector.service.pipeline.Pipeline;
import ru.yanin.shared.domain.TransactionEvent;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionConsumer {

    private final Pipeline pipeline;

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${app.kafka.topics.raw-transactions}",
            batch = "false"
    )
    public void consume(TransactionEvent event, Acknowledgment ack) {
        log.info("Consume event {}", event);
        try {
            pipeline.flow(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed event with id {} cause: {}", event.transactionId(), e.getMessage(), e);
        }
    }
}
