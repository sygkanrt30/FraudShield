package ru.yanin.dlq_worker.config;

import org.springframework.beans.factory.annotation.Value;
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

    private static final String THREAD_NAME = "transactions-producer-worker";

    @Bean(name = "producerTransactionsExecutor")
    public ExecutorService producerTransactionsExecutor(
            @Value("${executor.transaction.producer.nThread}") int nThreads) {
        return Executors.newFixedThreadPool(nThreads, r -> new Thread(r, THREAD_NAME));
    }
}
