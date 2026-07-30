package ru.yanin.dlq_worker.kafka.process.checks.handlers;

import lombok.extern.slf4j.Slf4j;
import ru.yanin.dlq_worker.kafka.process.checks.ProcessingContext;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
public class DuplicateHandler implements ProcessingHandler {

    @Override
    public boolean handle(ProcessingContext ctx) {
        if (ctx.stateStorage().isAlreadyProcessed(ctx.stringTxId())) {
            ctx.metrics().incrementDuplicate();
            log.debug("Transaction with id {} has already processed", ctx.txId());
            ctx.ack().acknowledge();
            return false;
        }

        if (ctx.stateStorage().isAlreadyInDeadQueue(ctx.stringTxId())) {
            log.debug("Transaction with id {} already in DQ", ctx.txId());
            ctx.ack().acknowledge();
            return false;
        }

        return true;
    }
}