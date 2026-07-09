package ru.yanin.system_ingress.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yanin.system_ingress.metrcis.Counters;
import ru.yanin.system_ingress.metrcis.Timers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

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
    public Map<String, Timer> timersMap(MeterRegistry registry) {
        Map<String, Timer> map = new ConcurrentHashMap<>();

        for (Timers value : Timers.values()) {
            Timer timer = value.builder().register(registry);
            map.put(value.name(), timer);
        }
        return map;
    }

    @Bean
    public LongAdder activeRequests(MeterRegistry registry) {
        LongAdder longAdder = new LongAdder();
        registry.gauge("ingress.active.requests", longAdder, LongAdder::sum);
        return longAdder;
    }
}
