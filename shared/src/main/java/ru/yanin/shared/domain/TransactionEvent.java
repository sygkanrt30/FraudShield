package ru.yanin.shared.domain;

import ru.yanin.shared.util.Validator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
public record TransactionEvent(
        UUID transactionId,
        Client from,
        Client to,
        BigDecimal amount,
        Currency currency,
        Instant createdAt
) {

    public TransactionEvent {
        Validator.validateNonNull(from, to, amount, currency, createdAt);
        if (amount.compareTo(BigDecimal.ZERO) < 1) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (createdAt.isAfter(Instant.now())) {
            throw new IllegalArgumentException("Created at is after now");
        }
    }
}
