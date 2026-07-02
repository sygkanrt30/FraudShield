package ru.yanin.ingress_service.metrcis;

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

    REQUESTS_TOTAL(io.micrometer.core.instrument.Counter.builder("ingress.requests.total")
            .description("Total number of incoming requests")
    ),
    REQUESTS_SUCCESS(io.micrometer.core.instrument.Counter.builder("ingress.requests.success")
            .description("Successful requests (HTTP 2xx)")
    ),
    REQUESTS_ERROR(io.micrometer.core.instrument.Counter.builder("ingress.requests.error")
            .description("Failed requests (HTTP 4xx, 5xx)")
    ),
    KAFKA_PUBLISH_SUCCESS(io.micrometer.core.instrument.Counter.builder("ingress.kafka.publish.success")
            .description("Successful Kafka publishes")
    ),
    KAFKA_PUBLISH_FAILURE(io.micrometer.core.instrument.Counter.builder("ingress.kafka.publish.failure")
            .description("Failed Kafka publishes")
    );

    private final Counter.Builder builder;
}
