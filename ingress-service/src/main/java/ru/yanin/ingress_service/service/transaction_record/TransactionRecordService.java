package ru.yanin.ingress_service.service.transaction_record;

import ru.yanin.ingress_service.model.dto.TransactionRequest;
import ru.yanin.ingress_service.model.entity.Status;
import ru.yanin.ingress_service.model.entity.TransactionRecord;

import java.util.List;
import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
public interface TransactionRecordService {

    void record(TransactionRequest request);

    void updateStatus(UUID transactionId, Status status);

    List<TransactionRecord> findByStatus(Status status);
}
