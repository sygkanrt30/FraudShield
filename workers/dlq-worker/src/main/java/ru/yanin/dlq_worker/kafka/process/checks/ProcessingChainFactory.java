package ru.yanin.dlq_worker.kafka.process.checks;

import ru.yanin.dlq_worker.kafka.process.checks.handlers.DbAvailabilityHandler;
import ru.yanin.dlq_worker.kafka.process.checks.handlers.DuplicateHandler;
import ru.yanin.dlq_worker.kafka.process.checks.handlers.ExistingTransactionHandler;
import ru.yanin.dlq_worker.kafka.process.checks.handlers.LockHandler;

/**
 * @author Vyacheslav Yanin
 */
final class ProcessingChainFactory {

    ProcessingChain defaultChain() {
        return new ProcessingChain()
                .addHandler(new DuplicateHandler())
                .addHandler(new LockHandler())
                .addHandler(new DbAvailabilityHandler())
                .addHandler(new ExistingTransactionHandler());
    }
}
