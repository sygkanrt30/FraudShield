package ru.yanin.ingress_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Vyacheslav Yanin
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    private static final String THREAD_NAME = "transactions-worker";

    @Bean(name = "producerTransactionsExecutor")
    public ExecutorService producerTransactionsExecutor() {
        return Executors.newFixedThreadPool(15, r -> {
            var t = new Thread(r, THREAD_NAME);
            t.setDaemon(true);
            return t;
        });
    }
}
