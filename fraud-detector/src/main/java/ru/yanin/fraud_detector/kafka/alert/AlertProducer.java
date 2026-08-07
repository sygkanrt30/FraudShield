package ru.yanin.fraud_detector.kafka.alert;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yanin.shared.alert.Alert;
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
public class AlertProducer implements Producer<Alert> {

    private final KafkaTemplate<String, Alert> kafkaTemplate;
    private final ExecutorService producerAlertExecutor;

    @Value("${app.kafka.topics.alert}")
    private String topic;

    @Override
    public void sendMessage(Alert alert) {
        CompletableFuture.runAsync(() -> kafkaTemplate.send(topic, alert), producerAlertExecutor)
                .whenComplete((result, throwable) -> {
                    if (Objects.isNull(throwable)) {
                        log.info("Alert {} sent", alert.getAlertType());
                    } else {
                        log.error("Kafka failed for alert {} cause: {}",
                                alert.getAlertType(), throwable.getMessage(), throwable);
                    }
                });
    }
}
