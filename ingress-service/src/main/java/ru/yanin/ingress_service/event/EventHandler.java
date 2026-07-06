package ru.yanin.ingress_service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.yanin.ingress_service.metrcis.IngressMetrics;
import ru.yanin.ingress_service.model.dto.event.TransactionEventWithTimer;
import ru.yanin.ingress_service.model.entity.Status;
import ru.yanin.ingress_service.service.transaction_record.TransactionRecordService;
import ru.yanin.shared.producer.Producer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * @author Vyacheslav Yanin
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EventHandler {

    private final Producer<TransactionEventWithTimer> producer;
    private final ExecutorService producerTransactionsExecutor;
    private final IngressMetrics metrics;
    private final TransactionRecordService transactionRecordService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTransactionEvent(TransactionEventWithTimer event) {
        CompletableFuture.runAsync(() -> producer.sendMessage(event),  producerTransactionsExecutor)
                .exceptionally(throwable -> {
                    log.error("Failed to send event after retries; eventId: {}",
                            event.transactionEvent().transactionId(), throwable);
                    transactionRecordService.updateStatus(event.transactionEvent().transactionId(), Status.INTERNAL_ERROR);
                    metrics.stopKafkaTimer(event.kafkaTimer());
                    return null;
                });
    }
}
