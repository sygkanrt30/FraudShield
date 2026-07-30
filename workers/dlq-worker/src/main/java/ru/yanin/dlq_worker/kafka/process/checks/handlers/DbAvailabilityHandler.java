package ru.yanin.dlq_worker.kafka.process.checks.handlers;

import lombok.extern.slf4j.Slf4j;
import ru.yanin.dlq_worker.kafka.process.checks.ProcessingContext;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
public class DbAvailabilityHandler implements ProcessingHandler {

    @Override
    public boolean handle(ProcessingContext ctx) {
        if (!ctx.dbAvailableChecker().isAvailable()) {
            try {
                ctx.metrics().incrementDBUnavailable();
                log.error("Database isn't available");
                return false;
            } finally {
                ctx.stateStorage().unlock(ctx.stringTxId());
            }
        }
        return true;
    }
}
