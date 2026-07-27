package ru.yanin.dlq_worker.repo;

import ru.yanin.shared.domain.TransactionEvent;

import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
public interface TransactionRepository {

    boolean save(TransactionEvent event, long offset);

    boolean existsById(UUID txId);
}
