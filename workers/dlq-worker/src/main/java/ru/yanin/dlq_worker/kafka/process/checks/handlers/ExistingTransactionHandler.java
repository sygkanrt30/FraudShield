package ru.yanin.dlq_worker.kafka.process.checks.handlers;

import lombok.extern.slf4j.Slf4j;
import ru.yanin.dlq_worker.kafka.process.checks.ProcessingContext;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
public class ExistingTransactionHandler implements ProcessingHandler {

    @Override
    public boolean handle(ProcessingContext ctx) {
        if (ctx.transactionService().transactionAlreadyExists(ctx.txId())) {
            try {
                ctx.stateStorage().markAsProcessed(ctx.stringTxId());
                ctx.metrics().incrementAlreadyExists();
                log.debug("Transaction with id {} has already in db", ctx.txId());
                ctx.ack().acknowledge();
                return false;
            } finally {
                ctx.stateStorage().unlock(ctx.stringTxId());
            }
        }
        return true;
    }
}
