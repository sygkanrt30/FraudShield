package ru.yanin.system_ingress.service.transaction_record;

import ru.yanin.system_ingress.model.dto.TransactionRequest;
import ru.yanin.system_ingress.model.entity.Status;
import ru.yanin.system_ingress.model.entity.TransactionRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
public interface TransactionRecordService {

    void record(TransactionRequest request);

    void updateStatus(UUID transactionId, Status status);

    void markTransactionExpiredIfNeeded(LocalDateTime lowerExpirationThreshold);

    List<TransactionRecord> findByStatus(Status status);
}
