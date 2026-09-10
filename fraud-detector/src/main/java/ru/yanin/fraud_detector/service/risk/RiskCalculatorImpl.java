package ru.yanin.fraud_detector.service.risk;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yanin.fraud_detector.dto.FraudMetricsByClient;
import ru.yanin.fraud_detector.dto.RiskScores;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskCalculatorImpl implements RiskCalculator {
    @Override
    public RiskScores compute(FraudMetricsByClient metricsByClient) {
        return null;
    }
}
