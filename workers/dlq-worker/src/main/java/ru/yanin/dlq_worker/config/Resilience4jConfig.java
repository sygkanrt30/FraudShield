package ru.yanin.dlq_worker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Vyacheslav Yanin
 */
@Configuration
public class Resilience4jConfig {

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newScheduledThreadPool(5,
                r -> new Thread(r, "resilience4j-worker"));
    }
}
