package ru.yanin.ingress_service.service.transaction_record;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yanin.ingress_service.model.dto.TransactionRequest;
import ru.yanin.ingress_service.model.entity.Client;
import ru.yanin.ingress_service.model.entity.Status;
import ru.yanin.ingress_service.model.entity.TransactionRecord;
import ru.yanin.ingress_service.repo.TransactionRecordRepository;
import ru.yanin.ingress_service.service.client.ClientService;

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
        Client to = clientService.save(request.toClientId(), request.toEmail(), request.toFullName());
        Client from = clientService.save(request.fromClientId(), request.fromEmail(), request.fromFullName());
        TransactionRecord transactionRecord = TransactionRecord.ofTransactionRequest(request);
        transactionRecord.setTo(to);
        transactionRecord.setFrom(from);

        transactionRecordRepository.save(transactionRecord);
        log.debug("Transaction record save successfully");
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
    public List<TransactionRecord> findByStatus(Status status) {
        return transactionRecordRepository.findByStatus(status);
    }
}
