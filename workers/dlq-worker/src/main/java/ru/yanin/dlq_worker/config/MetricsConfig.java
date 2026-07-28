package ru.yanin.dlq_worker.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yanin.dlq_worker.service.metrics.Counters;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Vyacheslav Yanin
 */
@Configuration
public class MetricsConfig {

    @Bean
    public Map<String, Counter> countersMap(MeterRegistry registry) {
        Map<String, Counter> map = new ConcurrentHashMap<>();

        for (Counters value : Counters.values()) {
            Counter counter = value.builder().register(registry);
            map.put(value.name(), counter);
        }
        return map;
    }

    @Bean
    public Timer insertTimer(MeterRegistry registry) {
        return Timer.builder("dlq.insert.duration")
                .description("Time taken to insert transaction into ClickHouse")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .sla(Duration.ofMillis(100), Duration.ofMillis(500), Duration.ofSeconds(1))
                .register(registry);
    }
}
