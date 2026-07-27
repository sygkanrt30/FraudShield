package ru.yanin.dlq_worker.service.state;

/**
 * Transaction state storage for DLQ worker.
 *
 * @author Vyacheslav Yanin
 */
public interface TransactionStateStorage {
    
    boolean isAlreadyProcessed(String txId);

    /**
     * @param ttlSeconds time-to-live after which it will be auto-deleted
     */
    void markAsProcessed(String txId, long ttlSeconds);

    /**
     * The flag will be stored for 24 hours (default value).
     */
    default void markAsProcessed(String txId) {
        markAsProcessed(txId, 86400); // 24 hours
    }
    
    int incrementAndGetRetryCount(String txId);
    
    void clearRetryCount(String txId);

    /**
     * Checks whether the transaction has already been sent to the DQ.
     *
     * @return {@code true} if the transaction is already in the DQ
     */
    boolean isAlreadyInDeadQueue(String txId);

    /**
     * Marks a transaction as sent to the DQ.
     */
    void markAsDead(String txId, long ttlSeconds);

    /**
     * Marks a transaction as sent to the DQ.
     * The flag will be stored for 7 days (default value).
     */
    default void markAsDead(String txId) {
        markAsDead(txId, 604800); // 7 days
    }

    /**
     * Attempts to acquire a distributed lock for processing the transaction.
     * <p>
     * Used to prevent parallel processing of the same transaction
     * by multiple worker instances.
     *
     * @param txId           transaction identifier
     * @param timeoutSeconds lock ttl in seconds
     * @return true if the lock was successfully acquired
     */
    boolean tryLock(String txId, long timeoutSeconds);

    /**
     * Releases the distributed lock.
     *
     */
    void unlock(String txId);
}
