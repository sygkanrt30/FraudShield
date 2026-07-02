package ru.yanin.ingress_service.metrcis;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Vyacheslav Yanin
 */
@Component
@RequiredArgsConstructor
public class IngressMetrics {

    private final Map<String, Counter> countersMap;
    private final Map<String, Timer> timersMap;
    private final LongAdder activeRequests;

    public void recordRequestStart() {
        activeRequests.increment();
        countersMap.get(Counters.REQUESTS_TOTAL.name()).increment();
    }

    public void recordRequestEnd(boolean success) {
        activeRequests.decrement();

        if (success) {
            countersMap.get(Counters.REQUESTS_SUCCESS.name()).increment();
        } else {
            countersMap.get(Counters.REQUESTS_ERROR.name()).increment();
        }
    }

    public Timer.Sample startTimer() {
        return Timer.start();
    }

    public void stopRequestTimer(Timer.Sample sample) {
        sample.stop(timersMap.get(Timers.REQUEST_DURATION.name()));
    }

    public void stopKafkaTimer(Timer.Sample sample) {
        sample.stop(timersMap.get(Timers.KAFKA_PUBLISH_DURATION.name()));
    }

    public void incrementKafkaPublishCounter(boolean success) {
        if (success) {
            countersMap.get(Counters.KAFKA_PUBLISH_SUCCESS.name()).increment();
        } else {
            countersMap.get(Counters.KAFKA_PUBLISH_FAILURE.name()).increment();
        }
    }
}
