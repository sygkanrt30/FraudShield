package ru.yanin.dlq_worker.repo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author Vyacheslav Yanin
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
enum SqlQuery {

    INSERT("""
            INSERT INTO transactions (
                txId, fromClientId, toClientId, amount,
                currency, timestamp, kafkaOffset
            ) VALUES (
                :txId, :fromClientId, :toClientId, :amount,
                :currency, :timestamp, :kafkaOffset
            )
            """),

    EXISTS_BY_ID(
            """
            SELECT COUNT(*) > 0
            FROM transactions
            WHERE txId = :txId
            """
    );

    private final String query;
}
