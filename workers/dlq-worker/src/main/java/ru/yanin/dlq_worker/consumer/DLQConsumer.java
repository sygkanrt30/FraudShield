package ru.yanin.dlq_worker.consumer;

import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import ru.yanin.dlq_worker.service.db.DBAvailableChecker;
import ru.yanin.dlq_worker.service.metrics.DLQMetrics;
import ru.yanin.dlq_worker.service.state.TransactionStateStorage;
import ru.yanin.dlq_worker.service.transaction.TransactionService;
import ru.yanin.shared.domain.TransactionEvent;
import ru.yanin.shared.producer.Producer;

import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DLQConsumer {

    private final TransactionStateStorage stateStorage;
    private final DLQMetrics metrics;
    private final TransactionService transactionService;
    private final DBAvailableChecker dbAvailableChecker;
    private final Producer<TransactionEvent> deadQueueProducer;

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${app.kafka.topics.dlq}",
            batch = "false"
    )
    public void consume(TransactionEvent event, Acknowledgment ack,
                        @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Consume event {}", event);
        UUID txId = event.transactionId();
        String stringTxId = toString();
        if (stateStorage.isAlreadyProcessed(stringTxId)) {
            metrics.incrementDuplicate();
            log.debug("Transaction with id {} has already processed", txId);
            ack.acknowledge();
            return;
        }

        if (stateStorage.isAlreadyInDeadQueue(stringTxId)) {
            log.debug("Transaction with id {} already in DQ", txId);
            ack.acknowledge();
            return;
        }

        boolean lockSuccess = stateStorage.tryLock(stringTxId, 60L);
        if (!lockSuccess) {
            metrics.incrementLockAcquisitionFailed();
            log.debug("Transaction with id {} has already in process", txId);
            return;
        }

        if (transactionService.transactionAlreadyExists(txId)) {
            try {
                stateStorage.markAsProcessed(stringTxId);
                metrics.incrementAlreadyExists();
                log.debug("Transaction with id {} has already in db", txId);
                ack.acknowledge();
                return;
            } finally {
                stateStorage.unlock(stringTxId);
            }
        }

        if (!dbAvailableChecker.isAvailable()) {
            try {
                metrics.incrementDBUnavailable();
                log.error("Database isn't available");
                return;
            } finally {
                stateStorage.unlock(stringTxId);
            }
        }

        Timer.Sample sample = metrics.startInsertTimer();
        boolean insertSuccess = transactionService.insertTransaction(event, offset);
        try {
            if (insertSuccess) {
                metrics.stopInsertTimer(sample);
                stateStorage.markAsProcessed(stringTxId);
                metrics.incrementProcessed();
                log.info("Transaction with id {} processed successfully", txId);
            } else {
                deadQueueProducer.sendMessage(event);
                stateStorage.markAsDead(stringTxId);
                metrics.incrementFailed();
                metrics.incrementDeadQueue();
                log.info("Transaction with id {} send to dead queue", txId);
            }
        } finally {
            ack.acknowledge();
            stateStorage.unlock(stringTxId);
        }
    }
}
