package ru.yanin.system_ingress.service.transaction_record;


import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yanin.system_ingress.model.dto.TransactionRequest;
import ru.yanin.system_ingress.model.entity.Status;
import ru.yanin.system_ingress.model.entity.TransactionRecord;
import ru.yanin.system_ingress.postgres.BasePostgresTest;
import ru.yanin.system_ingress.repo.ClientRepository;
import ru.yanin.system_ingress.repo.TransactionRecordRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * @author Vyacheslav Yanin
 */
@Tag("integration")
class TransactionRecordServiceImplTest extends BasePostgresTest {

    @Autowired
    private TransactionRecordService transactionRecordService;

    @Autowired
    private TransactionRecordRepository transactionRecordRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Test
    void shouldSuccessfullyRecordTransaction() {
        TransactionRequest request = createValidRequest();

        assertThatNoException().isThrownBy(() -> transactionRecordService.record(request));

        assertThat(transactionRecordRepository.findById(request.transactionId())).isPresent();
    }

    @Test
    void shouldBeIdempotentForClients() {
        TransactionRequest request = createValidRequest();

        transactionRecordService.record(request);
        transactionRecordService.record(request);

        assertThat(clientRepository.count()).isEqualTo(2);
        assertThat(transactionRecordRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldUpdateStatusSuccessfully() {
        TransactionRequest request = createValidRequest();
        transactionRecordService.record(request);

        assertThatNoException().isThrownBy(() ->
                transactionRecordService.updateStatus(request.transactionId(), Status.SENT_TO_KAFKA)
        );

        TransactionRecord record = transactionRecordRepository.findById(request.transactionId()).orElseThrow();
        assertThat(record.getStatus()).isEqualTo(Status.SENT_TO_KAFKA);
    }

    @Test
    void shouldNotThrowExceptionWhenUpdatingNonExistentTransaction() {
        UUID nonExistentId = UUID.randomUUID();

        assertThatNoException().isThrownBy(() ->
                transactionRecordService.updateStatus(nonExistentId, Status.KAFKA_ERROR)
        );
    }

    @Test
    void shouldHandleRepositoryExceptionGracefullyInUpdateStatus() {
        UUID transactionId = UUID.randomUUID();

        assertThatNoException().isThrownBy(() ->
                transactionRecordService.updateStatus(transactionId, Status.INTERNAL_ERROR)
        );
    }

    @Test
    void shouldFindByStatusReturnEmptyListWhenNoRecords() {
        List<TransactionRecord> result = transactionRecordService.findByStatus(Status.SENT_TO_KAFKA);

        assertThat(result).isEmpty();
    }

    private TransactionRequest createValidRequest() {
        return new TransactionRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Ivan Ivanov",
                "ivan@example.com",
                UUID.randomUUID(),
                "Anna Petrova",
                "anna@example.com",
                BigDecimal.valueOf(1500.00),
                "USD"
        );
    }
}