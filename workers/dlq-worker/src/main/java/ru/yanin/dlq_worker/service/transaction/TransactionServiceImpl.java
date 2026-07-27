package ru.yanin.dlq_worker.service.transaction;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yanin.dlq_worker.repo.TransactionRepository;
import ru.yanin.shared.domain.TransactionEvent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository repository;
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    @Override
    public boolean transactionAlreadyExists(UUID txId) {
        return repository.existsById(txId);
    }

    @Retry(name = "clickhouseInsert")
    @CircuitBreaker(name = "clickhouseInsert")
    @TimeLimiter(name = "clickhouseInsert")
    public CompletableFuture<Boolean> insertTransactionAsync(TransactionEvent event, long offset) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                boolean success = repository.save(event, offset);
                if (success) {
                    log.debug("Transaction {} inserted successfully", event.transactionId());
                }
                return success;
            } catch (Exception e) {
                log.error("Failed to insert transaction {}: {}", event.transactionId(), e.getMessage());
                throw new RuntimeException("Insert failed for txId: " + event.transactionId(), e);
            }
        }, executor);
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
                    .get();
        } catch (Exception e) {
            log.error("Insert failed for txId {}: {}", event.transactionId(), e.getMessage());
            return false;
        }
    }
}
