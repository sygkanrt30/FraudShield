package ru.yanin.dlq_worker.kafka.dq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import ru.yanin.dlq_worker.kafka.process.EventProcessor;
import ru.yanin.dlq_worker.service.metrics.DLQMetrics;

/**
 * @author Vyacheslav Yanin
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DQRecoveryConsumer {

    private EventProcessor eventProcessor;
    private final DLQMetrics metrics;

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${app.kafka.topics.dead}",
            batch = "false"
    )
    public void consume(DQTransactionEvent dqEvent, Acknowledgment ack, @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Consume event {} from DQ", dqEvent);
        try {
            eventProcessor.process(dqEvent.event(), ack, offset);
            metrics.incrementRecovered();
        } catch (Exception e) {
            log.error("Failed recovery event with id {} from DQ cause: {}",
                    dqEvent.event().transactionId(), e.getMessage(), e);
        }
    }

}
