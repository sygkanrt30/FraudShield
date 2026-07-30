package ru.yanin.dlq_worker.kafka.process.checks;

import ru.yanin.dlq_worker.kafka.process.checks.handlers.ProcessingHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vyacheslav Yanin
 */
public class ProcessingChain {

    private final List<ProcessingHandler> handlers = new ArrayList<>();

    public ProcessingChain addHandler(ProcessingHandler handler) {
        handlers.add(handler);
        return this;
    }

    public boolean process(ProcessingContext ctx) {
        for (ProcessingHandler handler : handlers) {
            boolean shouldContinue = handler.handle(ctx);
            if (!shouldContinue) {
                return false;
            }
        }
        return true;
    }
}
