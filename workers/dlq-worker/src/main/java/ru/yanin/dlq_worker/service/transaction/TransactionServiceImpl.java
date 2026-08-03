package ru.yanin.dlq_worker.service.transaction;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yanin.dlq_worker.repo.TransactionRepository;
import ru.yanin.shared.domain.TransactionEvent;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private static final String RESILIENCE4J_INSTANCE = "clickhouseInsert";
    private static final int TIMEOUT_SEC_ON_FUTURE = 20;

    private final TransactionRepository repository;
    private final RetryRegistry retryRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final TimeLimiterRegistry timeLimiterRegistry;
    private final ScheduledExecutorService scheduledExecutorService;
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    @Override
    public CompletableFuture<Boolean> insertTransactionAsync(TransactionEvent event, long offset) {
        Supplier<CompletionStage<Boolean>> baseOperation = getInsertEventAsyncSupplier(event, offset);
        return buildPipeline(baseOperation).get().toCompletableFuture();
    }

    private Supplier<CompletionStage<Boolean>> getInsertEventAsyncSupplier(TransactionEvent event, long offset) {
        return () ->
                CompletableFuture.supplyAsync(() -> {
                    try {
                        boolean result = repository.save(event, offset);
                        if (result) {
                            log.debug("Transaction {} inserted successfully", event.transactionId());
                        }
                        return result;
                    } catch (Exception e) {
                        log.error("Failed to insert transaction {}: {}", event.transactionId(), e.getMessage());
                        throw e;
                    }
                }, executor);
    }

    private Supplier<CompletionStage<Boolean>> buildPipeline(Supplier<CompletionStage<Boolean>> baseOperation) {
        Retry retry = retryRegistry.retry(RESILIENCE4J_INSTANCE);
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(RESILIENCE4J_INSTANCE);
        TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter(RESILIENCE4J_INSTANCE);

        return () ->
                retry.executeCompletionStage(scheduledExecutorService,
                        () -> circuitBreaker.executeCompletionStage(
                                () -> timeLimiter.executeCompletionStage(
                                        scheduledExecutorService,
                                        baseOperation
                                )
                        )
                );
    }

    @SuppressWarnings("unused")
    public CompletableFuture<Boolean> fallbackInsert(TransactionEvent event, long offset, Throwable t) {
        log.error("Fallback triggered for transaction {}: {}", event.transactionId(), t.getMessage(), t);
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public boolean insertTransaction(TransactionEvent event, long offset) {
        try {
            return insertTransactionAsync(event, offset)
                    .exceptionally(ex -> {
                        log.error("Exception in async insert: {}", ex.getMessage());
                        return false;
                    })
                    .get(TIMEOUT_SEC_ON_FUTURE, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.error("Insert timed out for txId {}: {}", event.transactionId(), e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Insert failed for txId {}: {}", event.transactionId(), e.getMessage());
            return false;
        }
    }

    @Override
    public boolean transactionAlreadyExists(UUID txId) {
        return repository.existsById(txId);
    }
}
