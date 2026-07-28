package ru.yanin.dlq_worker.kafka.dq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yanin.shared.domain.TransactionEvent;
import ru.yanin.shared.producer.Producer;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DQProducer implements Producer<TransactionEvent> {

    private final KafkaTemplate<String, DQTransactionEvent> kafkaTemplate;
    private final ExecutorService producerTransactionsExecutor;

    @Value("${app.kafka.topics.dead}")
    private String topic;

    @Override
    public void sendMessage(TransactionEvent event) {
        var dqEvent = new DQTransactionEvent(event, Instant.now());
        CompletableFuture.runAsync(() -> kafkaTemplate.send(topic, dqEvent), producerTransactionsExecutor)
                .whenComplete((result, throwable) -> {
                    if (Objects.isNull(throwable)) {
                        log.info("Transaction with id {} sent to DQ", event.transactionId());
                    } else {
                        log.error("Kafka failed for transaction with id {} cause: {}",
                                event.transactionId(), throwable.getMessage(), throwable);
                    }
                });
    }
}
