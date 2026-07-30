package ru.yanin.dlq_worker.kafka.process.checks;

import ru.yanin.dlq_worker.kafka.process.checks.handlers.ProcessingHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vyacheslav Yanin
 */
class ProcessingChain {

    private final List<ProcessingHandler> handlers = new ArrayList<>();

    ProcessingChain addHandler(ProcessingHandler handler) {
        handlers.add(handler);
        return this;
    }

    boolean process(ProcessingContext ctx) {
        for (ProcessingHandler handler : handlers) {
            boolean shouldContinue = handler.handle(ctx);
            if (!shouldContinue) {
                return false;
            }
        }
        return true;
    }
}
