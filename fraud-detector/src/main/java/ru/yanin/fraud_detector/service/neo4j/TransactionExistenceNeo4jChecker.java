package ru.yanin.fraud_detector.service.neo4j;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yanin.fraud_detector.repo.neo4j.ClientNeo4jRepository;
import ru.yanin.shared.domain.TransactionEvent;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionExistenceNeo4jChecker implements TransactionExistenceChecker {

    private final ClientNeo4jRepository clientNeo4JRepository;

    @Override
    @Retry(name = "transactionExistsRetry")
    public boolean isTransactionAlreadySavedWithRetry(TransactionEvent transaction) {
        return clientNeo4JRepository.existsByTransactionId(transaction.transactionId());
    }

    @SuppressWarnings("unused")
    private boolean transactionNotFoundFallback(String transactionId, Throwable t) {
        log.warn("Transaction {} still not found after all retries. Cause: {}",
                transactionId, t.toString());
        return false;
    }
}
