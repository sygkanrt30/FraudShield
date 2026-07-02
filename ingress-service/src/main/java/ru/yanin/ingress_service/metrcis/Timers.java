package ru.yanin.ingress_service.metrcis;

import io.micrometer.core.instrument.Timer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author Vyacheslav Yanin
 */
@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public enum Timers {

    REQUEST_DURATION(io.micrometer.core.instrument.Timer.builder("ingress.requests.duration")
            .description("Request processing duration")
    ),
    KAFKA_PUBLISH_DURATION(io.micrometer.core.instrument.Timer.builder("ingress.kafka.publish.duration")
            .description("Kafka publish duration")
    );

    private final Timer.Builder builder;
}
