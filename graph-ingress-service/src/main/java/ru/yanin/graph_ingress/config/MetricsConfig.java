package ru.yanin.graph_ingress.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Vyacheslav Yanin
 */
@Configuration
public class MetricsConfig {

    @Bean
    public Counter errorCounter(MeterRegistry meterRegistry) {
        return Counter.builder("kafka.consumer.transaction.errors")
                .description("Total processing errors in raw transaction consumer")
                .tag("service", "graph-ingress")
                .register(meterRegistry);
    }
}
