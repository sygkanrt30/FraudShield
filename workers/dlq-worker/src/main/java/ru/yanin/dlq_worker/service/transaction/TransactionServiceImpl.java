package ru.yanin.dlq_worker.service.transaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yanin.shared.domain.TransactionEvent;

import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    @Override
    public boolean transactionAlreadyExists(UUID txId) {
        return false;
    }

    @Override
    public boolean insertTransaction(TransactionEvent event) {
        return false;
    }
}
