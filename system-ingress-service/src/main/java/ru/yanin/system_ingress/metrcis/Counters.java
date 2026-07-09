package ru.yanin.system_ingress.metrcis;

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

    TRANSACTIONS_RECEIVED(Counter.builder("ingress.transactions.received")),

    TRANSACTIONS_PROCESSED(Counter.builder("ingress.transactions.processed")),

    TRANSACTIONS_FAILED(Counter.builder("ingress.transactions.failed")),

    KAFKA_PUBLISH_SUCCESS(Counter.builder("ingress.kafka.publish.success").description("Successful Kafka publishes")),

    KAFKA_PUBLISH_FAILURE(Counter.builder("ingress.kafka.publish.failure").description("Failed Kafka publishes"));

    private final Counter.Builder builder;
}
