package ru.yanin.dlq_worker.repo;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yanin.dlq_worker.clickhouse.BaseClickhouseTest;
import ru.yanin.shared.domain.ClientDto;
import ru.yanin.shared.domain.Currency;
import ru.yanin.shared.domain.TransactionEvent;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vyacheslav Yanin
 */
@Tag("integration")
class ClickHouseTransactionRepositoryTest extends BaseClickhouseTest {

    @Autowired
    private ClickHouseTransactionRepository repository;

    @Test
    void save_shouldReturnTrue_whenTransactionIsValid() {
        var event = randomTransactionEvent();
        long offset = 42L;

        // Act
        boolean saved = repository.save(event, offset);

        // Assert
        assertThat(saved).isTrue();
        assertThat(repository.existsById(event.transactionId())).isTrue();
    }

    @Test
    void save_shouldPersistCorrectData() {
        var event = randomTransactionEvent();
        long offset = 100L;

        // Act
        repository.save(event, offset);

        // Assert
        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT * FROM transactions WHERE txId = ?",
                event.transactionId().toString()
        );

        assertThat(row).isNotNull();
        assertThat(row.get("txId")).isEqualTo(event.transactionId().toString());
        assertThat(row.get("fromClientId")).isEqualTo(event.from().id().toString());
        assertThat(row.get("toClientId")).isEqualTo(event.to().id().toString());
        assertThat(new BigDecimal(row.get("amount").toString()))
                .isEqualByComparingTo(event.amount());
        assertThat(row.get("currency")).isEqualTo(event.currency().toString());
        assertThat(row.get("kafkaOffset")).isEqualTo(BigInteger.valueOf(offset));
        assertThat(row.get("timestamp")).isNotNull();
    }

    @Test
    void existsById_shouldReturnTrue_whenTransactionExists() {
        var event = randomTransactionEvent();
        repository.save(event, 1L);

        // Act
        boolean exists = repository.existsById(event.transactionId());

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_shouldReturnFalse_whenTransactionDoesNotExist() {
        UUID unknownId = UUID.randomUUID();

        // Act
        boolean exists = repository.existsById(unknownId);

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void save_shouldReturnTrue_onDuplicateInsert() {
        var event = randomTransactionEvent();
        long offset = 999L;

        // Act
        boolean first = repository.save(event, offset);
        boolean second = repository.save(event, offset);

        // Assert
        assertThat(first).isTrue();
        assertThat(second).isTrue();
    }

    private TransactionEvent randomTransactionEvent() {
        return new TransactionEvent(
                UUID.randomUUID(),
                new ClientDto(UUID.randomUUID(), "sender@example.com", "Sender Name"),
                new ClientDto(UUID.randomUUID(), "receiver@example.com", "Receiver Name"),
                BigDecimal.valueOf(99.99),
                Currency.USD,
                Instant.now().minusSeconds(5)
        );
    }
}