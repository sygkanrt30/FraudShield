package ru.yanin.dlq_worker.repo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yanin.shared.domain.TransactionEvent;

import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ClickHouseTransactionRepository implements TransactionRepository {

    private NamedParameterJdbcTemplate jdbc;

    @Override
    @Transactional
    public boolean save(TransactionEvent event, long offset) {
        var params = new MapSqlParameterSource()
                .addValue("txId", event.transactionId().toString())
                .addValue("fromClientId", event.from().id())
                .addValue("toClientId", event.to().id())
                .addValue("amount", event.amount())
                .addValue("currency", event.currency())
                .addValue("timestamp", event.createdAt())
                .addValue("kafkaOffset", offset);

        int affectedRows = jdbc.update(SqlQuery.INSERT.query(), params);
        return isSaved(event, affectedRows);
    }

    private boolean isSaved(TransactionEvent event, int affectedRows) {
        if (affectedRows == 1) {
            log.trace("Transaction with id {} saved in CH", event.transactionId());
            return true;
        }
        return false;
    }

    @Override
    public boolean existsById(UUID txId) {
        var params = new MapSqlParameterSource()
                .addValue("txId", txId.toString());
        return Boolean.TRUE.equals(jdbc.queryForObject(SqlQuery.EXISTS_BY_ID.query(), params, Boolean.class));
    }
}
