package ru.yanin.system_ingress.service.transaction_record;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yanin.system_ingress.model.dto.TransactionRequest;
import ru.yanin.system_ingress.model.entity.Client;
import ru.yanin.system_ingress.model.entity.Status;
import ru.yanin.system_ingress.model.entity.TransactionRecord;
import ru.yanin.system_ingress.repo.TransactionRecordRepository;
import ru.yanin.system_ingress.service.client.ClientService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionRecordServiceImpl implements TransactionRecordService {

    private final TransactionRecordRepository transactionRecordRepository;
    private final ClientService clientService;

    @Override
    @Transactional
    public void record(TransactionRequest request) {
        validateTransaction(request);

        Client to = clientService.upsertClient(request.toClientId(), request.toEmail(), request.toFullName());
        Client from = clientService.upsertClient(request.fromClientId(), request.fromEmail(), request.fromFullName());

        TransactionRecord transactionRecord = TransactionRecord.ofTransactionRequest(request);
        transactionRecord.setTo(to);
        transactionRecord.setFrom(from);

        transactionRecordRepository.save(transactionRecord);
        log.debug("Transaction record save successfully");
    }

    private void validateTransaction(TransactionRequest request) {
        transactionRecordRepository.findById(request.transactionId())
                .ifPresent(transactionRecord -> {
                    throw new IllegalArgumentException(
                            String.format("Transaction record with id %s already exists",
                                    request.transactionId())
                    );
                });
    }

    @Override
    @Transactional
    public void updateStatus(UUID transactionId, Status status) {
        try {
            transactionRecordRepository.updateStatus(transactionId, status);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void markTransactionExpiredIfNeeded(Instant lowerExpirationThreshold) {
        transactionRecordRepository
                .updateStatusForLongPendingTransactions(lowerExpirationThreshold, Status.EXPIRED, Status.PENDING);
    }

    @Override
    public List<TransactionRecord> findByStatus(Status status) {
        return transactionRecordRepository.findByStatus(status);
    }
}
