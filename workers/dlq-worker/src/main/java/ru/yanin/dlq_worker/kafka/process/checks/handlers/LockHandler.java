package ru.yanin.dlq_worker.kafka.process.checks.handlers;

import lombok.extern.slf4j.Slf4j;
import ru.yanin.dlq_worker.kafka.process.checks.ProcessingContext;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
public class LockHandler implements ProcessingHandler {

    private static final long TIMEOUT_SECONDS = 60L;

    @Override
    public boolean handle(ProcessingContext ctx) {
        boolean lockSuccess = ctx.stateStorage().tryLock(ctx.stringTxId(), TIMEOUT_SECONDS);
        if (!lockSuccess) {
            ctx.metrics().incrementLockAcquisitionFailed();
            log.debug("Transaction with id {} has already in process", ctx.txId());
            return false;
        }
        return true;
    }
}
