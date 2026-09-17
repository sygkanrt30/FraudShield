package ru.yanin.fraud_detector.kafka.manualreview;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yanin.shared.domain.TransactionEvent;
import ru.yanin.shared.producer.Producer;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ManualReviewProducer implements Producer<TransactionEvent> {

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;
    private final ExecutorService producerAlertExecutor;

    @Value("${app.kafka.topics.manual-review}")
    private String topic;

    @Override
    public void sendMessage(TransactionEvent event) {
        CompletableFuture.runAsync(() -> kafkaTemplate.send(topic, event), producerAlertExecutor)
                .whenComplete((result, throwable) -> {
                    if (Objects.isNull(throwable)) {
                        log.info("Transaction {} sent to manual review", event.transactionId());
                    } else {
                        log.error("Kafka failed to send transaction {} to manual review cause: {}",
                                event.transactionId(), throwable.getMessage(), throwable);
                    }
                });
    }
}