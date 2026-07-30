package ru.yanin.dlq_worker.kafka.process.checks.util;

import ru.yanin.dlq_worker.kafka.process.checks.ProcessingChain;
import ru.yanin.dlq_worker.kafka.process.checks.handlers.DbAvailabilityHandler;
import ru.yanin.dlq_worker.kafka.process.checks.handlers.DuplicateHandler;
import ru.yanin.dlq_worker.kafka.process.checks.handlers.ExistingTransactionHandler;
import ru.yanin.dlq_worker.kafka.process.checks.handlers.LockHandler;

/**
 * @author Vyacheslav Yanin
 */
public final class ProcessingChainFactory {

    public ProcessingChain defaultChain() {
        return new ProcessingChain()
                .addHandler(new DuplicateHandler())
                .addHandler(new LockHandler())
                .addHandler(new DbAvailabilityHandler())
                .addHandler(new ExistingTransactionHandler());
    }
}
