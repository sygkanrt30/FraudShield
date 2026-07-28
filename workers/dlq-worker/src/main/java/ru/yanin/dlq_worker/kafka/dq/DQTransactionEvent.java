package ru.yanin.dlq_worker.kafka.dq;

import ru.yanin.shared.domain.TransactionEvent;

import java.time.Instant;

/**
 * @author Vyacheslav Yanin
 */
public record DQTransactionEvent(TransactionEvent event, Instant timestamp) {
}
