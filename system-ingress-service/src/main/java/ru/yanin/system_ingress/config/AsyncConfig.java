package ru.yanin.system_ingress.config;

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
        return Executors.newFixedThreadPool(15, r -> new Thread(r, THREAD_NAME));
    }
}
