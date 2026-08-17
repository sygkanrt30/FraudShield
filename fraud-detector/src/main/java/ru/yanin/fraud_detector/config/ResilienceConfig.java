package ru.yanin.fraud_detector.config;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * @author Vyacheslav Yanin
 */
@Configuration
public class ResilienceConfig {

    @Bean
    public RetryRegistry retryRegistry(
            @Value("${resilience4j.retry.initial-interval}") int interval,
            @Value("${resilience4j.retry.multiplier}") double multiplier) {

        var initialInterval = Duration.ofMillis(interval);
        var config = RetryConfig.custom()
                .maxAttempts(4)
                .waitDuration(initialInterval)
                .intervalFunction(IntervalFunction.ofExponentialBackoff(initialInterval, multiplier))
                .retryOnResult(Boolean.FALSE::equals)
                .build();

        return RetryRegistry.of(config);
    }
}
