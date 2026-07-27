package ru.yanin.dlq_worker.service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Metrics service for DLQ worker monitoring.
 *
 * @author Vyacheslav Yanin
 */
@Service
@Slf4j
public class DLQMetrics {

    private final Counter processedTotal;
    private final Counter failedTotal;
    private final Counter duplicateTotal;
    private final Counter alreadyExistsTotal;
    private final Counter recoveredTotal;
    private final Counter chUnavailableTotal;
    private final Counter deadQueueTotal;
    private final Counter lockAcquisitionFailedTotal;

    private final AtomicLong queueSize = new AtomicLong(0L);
    private final AtomicLong deadQueueSize = new AtomicLong(0L);
    private final AtomicLong retryCount = new AtomicLong(0L);

    private final Timer insertTimer;

    public DLQMetrics(MeterRegistry meterRegistry) {
        this.processedTotal = Counter.builder("dlq.processed.total")
                .description("Total transactions successfully recovered from DLQ")
                .register(meterRegistry);

        this.failedTotal = Counter.builder("dlq.failed.total")
                .description("Total transactions that failed processing")
                .register(meterRegistry);

        this.duplicateTotal = Counter.builder("dlq.duplicate.total")
                .description("Total duplicate transactions skipped")
                .register(meterRegistry);

        this.alreadyExistsTotal = Counter.builder("dlq.already.exists.total")
                .description("Total transactions already present in ClickHouse")
                .register(meterRegistry);

        this.recoveredTotal = Counter.builder("dlq.recovered.total")
                .description("Total transactions recovered from dead queue")
                .register(meterRegistry);

        this.chUnavailableTotal = Counter.builder("dlq.ch.unavailable.total")
                .description("Total ClickHouse unavailable events")
                .register(meterRegistry);

        this.deadQueueTotal = Counter.builder("dlq.dead.queue.total")
                .description("Total transactions moved to dead queue")
                .register(meterRegistry);

        this.lockAcquisitionFailedTotal = Counter.builder("dlq.lock.acquisition.failed.total")
                .description("Total distributed lock acquisition failures")
                .register(meterRegistry);

        meterRegistry.gauge("dlq.queue.size", queueSize);
        meterRegistry.gauge("dlq.dead.queue.size", deadQueueSize);
        meterRegistry.gauge("dlq.retry.count", retryCount);

        this.insertTimer = Timer.builder("dlq.insert.duration")
                .description("Time taken to insert transaction into ClickHouse")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .sla(Duration.ofMillis(100), Duration.ofMillis(500), Duration.ofSeconds(1))
                .register(meterRegistry);
    }


    public void incrementProcessed() {
        processedTotal.increment();
    }

    public void incrementFailed() {
        failedTotal.increment();
    }

    public void incrementDuplicate() {
        duplicateTotal.increment();
    }

    public void incrementAlreadyExists() {
        alreadyExistsTotal.increment();
    }

    public void incrementRecovered() {
        recoveredTotal.increment();
    }

    public void incrementDBUnavailable() {
        chUnavailableTotal.increment();
    }

    public void incrementDeadQueue() {
        deadQueueTotal.increment();
    }

    public void incrementLockAcquisitionFailed() {
        lockAcquisitionFailedTotal.increment();
    }

    public void setQueueSize(int size) {
        queueSize.set(size);
    }

    public void setDeadQueueSize(int size) {
        deadQueueSize.set(size);
    }

    public void setRetryCount(int count) {
        retryCount.set(count);
    }

    public Timer.Sample startInsertTimer() {
        return Timer.start();
    }

    public void stopInsertTimer(Timer.Sample sample) {
        sample.stop(insertTimer);
    }
}