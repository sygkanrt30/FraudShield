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

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Component
@RequiredArgsConstructor
public final class InsertionHandler {

    private final DLQMetrics metrics;
    private final TransactionService transactionService;
    private final TransactionStateStorage stateStorage;
    private final Producer<TransactionEvent> deadQueueProducer;

    public void handle(TransactionEvent event, Acknowledgment ack, long offset) {
        Timer.Sample sample = metrics.startInsertTimer();

        boolean insertSuccess = transactionService.insertTransaction(event, offset);
        String stringTxId = event.transactionId().toString();
        try {
            if (insertSuccess) {
                markAsProcessed(sample, stringTxId);
            } else {
                sendToDQ(event, stringTxId);
            }
        } finally {
            ack.acknowledge();
            stateStorage.unlock(stringTxId);
        }
    }

    private void markAsProcessed(Timer.Sample sample, String stringTxId) {
        metrics.stopInsertTimer(sample);
        stateStorage.markAsProcessed(stringTxId);
        metrics.incrementProcessed();
        log.info("Transaction with id {} processed successfully", stringTxId);
    }

    private void sendToDQ(TransactionEvent event, String stringTxId) {
        stateStorage.markAsDead(stringTxId);
        deadQueueProducer.sendMessage(event);
        log.info("Transaction with id {} send to dead queue", stringTxId);
    }
}
