package ru.yanin.fraud_detector.service.clickhouse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yanin.fraud_detector.dto.RiskScores;
import ru.yanin.fraud_detector.model.clickhouse.TransactionStatus;
import ru.yanin.fraud_detector.repo.clickhouse.ClickHouseRepository;
import ru.yanin.shared.domain.TransactionEvent;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClickHouseStatusUpdaterImpl implements ClickHouseStatusUpdater {

    private static final double BLOCK_THRESHOLD = 0.6;

    private final ClickHouseRepository clickHouseRepository;

    @Override
    public void updateStatus(TransactionEvent transaction, RiskScores riskScores) {
        boolean isFraud = riskScores.overallRisk() > BLOCK_THRESHOLD;
        var status = isFraud ? TransactionStatus.BLOCKED : TransactionStatus.CHECKED;
        String fraudReason = isFraud ? riskScores.riskCategory().name() : "";
        clickHouseRepository.updateStatus(transaction, status, isFraud, riskScores.overallRisk(), fraudReason);
        log.debug("Updated transaction {} status to {} (risk={}, reason={})",
                transaction.transactionId(), status, riskScores.overallRisk(), fraudReason);
    }
}
