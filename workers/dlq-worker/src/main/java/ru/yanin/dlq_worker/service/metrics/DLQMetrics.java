package ru.yanin.dlq_worker.service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Metrics service for DLQ worker monitoring.
 *
 * @author Vyacheslav Yanin
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DLQMetrics {

    private final Map<String, Counter> countersMap;
    private final Timer insertTimer;

    public void incrementProcessed() {
        getCounter(Counters.DLQ_PROCESSED_TOTAL).increment();
    }

    public void incrementFailed() {
        getCounter(Counters.DLQ_FAILED_TOTAL).increment();
    }

    public void incrementDuplicate() {
        getCounter(Counters.DLQ_DUPLICATE_TOTAL).increment();
    }

    public void incrementAlreadyExists() {
        getCounter(Counters.DLQ_ALREADY_EXISTS_TOTAL).increment();
    }

    public void incrementDBUnavailable() {
        getCounter(Counters.DLQ_CH_UNAVAILABLE_TOTAL).increment();
    }

    public void incrementDeadQueue() {
        getCounter(Counters.DLQ_DEAD_QUEUE_TOTAL).increment();
    }

    public void incrementLockAcquisitionFailed() {
        getCounter(Counters.DLQ_LOCK_ACQUISITION_FAILED_TOTAL).increment();
    }

    public Timer.Sample startInsertTimer() {
        return Timer.start();
    }

    public void stopInsertTimer(Timer.Sample sample) {
        sample.stop(insertTimer);
    }

    private Counter getCounter(Counters counterEnum) {
        return countersMap.get(counterEnum.name());
    }
}