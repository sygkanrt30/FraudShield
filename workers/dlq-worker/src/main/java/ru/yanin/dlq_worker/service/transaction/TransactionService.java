package ru.yanin.dlq_worker.service.transaction;

import ru.yanin.shared.domain.TransactionEvent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Vyacheslav Yanin
 */
public interface TransactionService {

    boolean transactionAlreadyExists(UUID txId);

    /**
     * @return {@code true} if transaction insert successfully
     */
    boolean insertTransaction(TransactionEvent event, long offset);

    CompletableFuture<Boolean> insertTransactionAsync(TransactionEvent event, long offset);
}
