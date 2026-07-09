package ru.yanin.system_ingress.metrcis;

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

    KAFKA_PUBLISH_DURATION(Timer.builder("ingress.kafka.publish.duration").description("Kafka publish duration"));

    private final Timer.Builder builder;
}
