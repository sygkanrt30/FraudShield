package ru.yanin.dlq_worker.kafka.process.checks.handlers;

import ru.yanin.dlq_worker.kafka.process.checks.ProcessingContext;

/**
 * Defines a contract for individual processing steps in the validation pipeline.
 *
 * @author Vyacheslav Yanin
 */
public interface ProcessingHandler {

    /**
     * Processes the step and returns true if the chain should continue.
     */
    boolean handle(ProcessingContext context);
}
