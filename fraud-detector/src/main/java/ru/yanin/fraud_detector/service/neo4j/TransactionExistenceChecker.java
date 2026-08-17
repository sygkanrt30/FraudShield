package ru.yanin.fraud_detector.service.neo4j;

import ru.yanin.shared.domain.TransactionEvent;

/**
 * @author Vyacheslav Yanin
 */
public interface TransactionExistenceChecker {

    boolean isTransactionAlreadySavedWithRetry(TransactionEvent transaction);
}
