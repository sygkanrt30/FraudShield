package ru.yanin.dlq_worker.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import ru.yanin.dlq_worker.kafka.process.EventProcessor;
import ru.yanin.dlq_worker.service.metrics.DLQMetrics;
import ru.yanin.shared.domain.TransactionEvent;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DLQConsumer {

    private final DLQMetrics metrics;
    private EventProcessor eventProcessor;

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${app.kafka.topics.dlq}",
            batch = "false"
    )
    public void consume(TransactionEvent event, Acknowledgment ack, @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Consume event {}", event);
        try {
            eventProcessor.process(event, ack, offset);
        } catch (Exception e) {
            metrics.incrementFailed();
            log.error("Failed event with id {} cause: {}", event.transactionId(), e.getMessage(), e);
        }
    }
}
