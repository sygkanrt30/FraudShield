package ru.yanin.graph_ingress.event;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.yanin.graph_ingress.service.GraphTransactionPersister;
import ru.yanin.shared.domain.TransactionEvent;

import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventKafkaConsumer {

    private final Counter errorCounter;
    private final GraphTransactionPersister persister;

    @Counted(value = "kafka.consumer.transaction.received", description = "Total transactions received")
    @Timed(
            value = "kafka.consumer.transaction",
            description = "Time spent processing transaction from Kafka",
            percentiles = {0.5, 0.95, 0.99}
    )
    @KafkaListener(
            topics = "${app.kafka.topics.raw-transactions}",
            groupId = "${app.kafka.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(TransactionEvent event) {
        UUID transactionId = event.transactionId();
        try {
            log.info("Received transaction from Kafka: {} | Thread: {}",
                    transactionId, Thread.currentThread().getName());

            persister.persistTransaction(event);

            log.info("Successfully processed transaction: {}", transactionId);
        } catch (Exception e) {
            log.error("Failed to process transaction: {}", transactionId, e);
            errorCounter.increment();
            throw e;
        }
    }
}
