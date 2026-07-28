package ru.yanin.dlq_worker.kafka.process;

import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
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
final class InsertionHandler {

    private final DLQMetrics metrics;
    private final TransactionService transactionService;
    private final TransactionStateStorage stateStorage;
    private final Producer<TransactionEvent> deadQueueProducer;

    void handle(TransactionEvent event, Acknowledgment ack, long offset) {
        UUID txId = event.transactionId();
        String stringTxId = txId.toString();
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
