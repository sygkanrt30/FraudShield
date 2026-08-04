package ru.yanin.dlq_worker.service.metrics;

import io.micrometer.core.instrument.Counter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author Vyacheslav Yanin
 */
@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public enum Counters {

    DLQ_PROCESSED_TOTAL(Counter.builder("dlq.processed.total")
            .description("Total transactions successfully recovered from DLQ")),

    DLQ_FAILED_TOTAL(Counter.builder("dlq.failed.total")
            .description("Total transactions that failed processing")),

    DLQ_DUPLICATE_TOTAL(Counter.builder("dlq.duplicate.total")
            .description("Total duplicate transactions skipped")),

    DLQ_ALREADY_EXISTS_TOTAL(Counter.builder("dlq.already.exists.total")
            .description("Total transactions already present in ClickHouse")),

    DLQ_CH_UNAVAILABLE_TOTAL(Counter.builder("dlq.ch.unavailable.total")
            .description("Total ClickHouse unavailable events")),

    DLQ_DEAD_QUEUE_TOTAL(Counter.builder("dlq.dead.queue.total")
            .description("Total transactions moved to dead queue")),

    DLQ_LOCK_ACQUISITION_FAILED_TOTAL(Counter.builder("dlq.lock.acquisition.failed.total")
            .description("Total distributed lock acquisition failures"));

    private final Counter.Builder builder;
}
