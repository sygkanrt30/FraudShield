package ru.yanin.dlq_worker.kafka.process;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.yanin.dlq_worker.service.db.DBAvailableChecker;
import ru.yanin.dlq_worker.service.metrics.DLQMetrics;
import ru.yanin.dlq_worker.service.state.TransactionStateStorage;
import ru.yanin.dlq_worker.service.transaction.TransactionService;

import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Component
@RequiredArgsConstructor
final class PipelineChecks {

    private final TransactionStateStorage stateStorage;
    private final DLQMetrics metrics;
    private final DBAvailableChecker dbAvailableChecker;
    private final TransactionService transactionService;

    boolean flow(UUID txId, Acknowledgment ack) {
        String stringTxId = txId.toString();
        if (stateStorage.isAlreadyProcessed(stringTxId)) {
            metrics.incrementDuplicate();
            log.debug("Transaction with id {} has already processed", txId);
            ack.acknowledge();
            return false;
        }

        if (stateStorage.isAlreadyInDeadQueue(stringTxId)) {
            log.debug("Transaction with id {} already in DQ", txId);
            ack.acknowledge();
            return false;
        }

        boolean lockSuccess = stateStorage.tryLock(stringTxId, 60L);
        if (!lockSuccess) {
            metrics.incrementLockAcquisitionFailed();
            log.debug("Transaction with id {} has already in process", txId);
            return false;
        }

        if (!dbAvailableChecker.isAvailable()) {
            try {
                metrics.incrementDBUnavailable();
                log.error("Database isn't available");
                return false;
            } finally {
                stateStorage.unlock(stringTxId);
            }
        }

        if (transactionService.transactionAlreadyExists(txId)) {
            try {
                stateStorage.markAsProcessed(stringTxId);
                metrics.incrementAlreadyExists();
                log.debug("Transaction with id {} has already in db", txId);
                ack.acknowledge();
                return false;
            } finally {
                stateStorage.unlock(stringTxId);
            }
        }
        return true;
    }
}
