package ru.yanin.fraud_detector.service.clickhouse;

import ru.yanin.fraud_detector.dto.RiskScores;
import ru.yanin.shared.domain.TransactionEvent;

/**
 * @author Vyacheslav Yanin
 */
public interface ClickHouseStatusUpdater {

    void updateStatus(TransactionEvent transaction, RiskScores riskScores);
}
