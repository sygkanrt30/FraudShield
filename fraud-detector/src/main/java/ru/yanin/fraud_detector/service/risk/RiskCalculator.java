package ru.yanin.fraud_detector.service.risk;

import ru.yanin.fraud_detector.dto.FraudMetricsByClient;
import ru.yanin.fraud_detector.dto.RiskScores;

/**
 * @author Vyacheslav Yanin
 */
public interface RiskCalculator {

    RiskScores compute(FraudMetricsByClient metricsByClient);
}
