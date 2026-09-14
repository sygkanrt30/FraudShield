package ru.yanin.fraud_detector.service.risk;

import ru.yanin.fraud_detector.model.clickhouse.FraudMetrics;
import ru.yanin.fraud_detector.dto.RiskScores;

/**
 * @author Vyacheslav Yanin
 */
public interface RiskCalculator {

    RiskScores compute(FraudMetrics fraudMetrics);
}
