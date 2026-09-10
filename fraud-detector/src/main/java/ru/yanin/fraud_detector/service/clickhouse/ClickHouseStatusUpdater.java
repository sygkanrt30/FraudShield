package ru.yanin.fraud_detector.service.clickhouse;

import ru.yanin.fraud_detector.dto.RiskScores;

/**
 * @author Vyacheslav Yanin
 */
public interface ClickHouseStatusUpdater {

    void updateStatus(RiskScores riskScores);
}
