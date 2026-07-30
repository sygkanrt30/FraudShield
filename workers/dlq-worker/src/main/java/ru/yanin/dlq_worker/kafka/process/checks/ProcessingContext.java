package ru.yanin.dlq_worker.kafka.process.checks;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.yanin.dlq_worker.service.db.DBAvailableChecker;
import ru.yanin.dlq_worker.service.metrics.DLQMetrics;
import ru.yanin.dlq_worker.service.state.TransactionStateStorage;
import ru.yanin.dlq_worker.service.transaction.TransactionService;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
@Component
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class ProcessingContext {

    private final TransactionStateStorage stateStorage;
    private final DLQMetrics metrics;
    private final TransactionService transactionService;
    private final DBAvailableChecker dbAvailableChecker;

    private UUID txId;
    private Acknowledgment ack;

    public String stringTxId() {
        if (Objects.nonNull(txId)) {
            return txId.toString();
        }
        return null;
    }

    public ProcessingContext setTxId(UUID txId) {
        this.txId = txId;
        return this;
    }

    public ProcessingContext setAck(Acknowledgment ack) {
        this.ack = ack;
        return this;
    }

}
