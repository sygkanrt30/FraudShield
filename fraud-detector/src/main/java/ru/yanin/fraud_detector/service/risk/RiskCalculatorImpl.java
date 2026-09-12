package ru.yanin.fraud_detector.service.risk;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yanin.fraud_detector.dto.FraudMetrics;
import ru.yanin.fraud_detector.dto.RiskScores;

import java.math.BigDecimal;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskCalculatorImpl implements RiskCalculator {

    @Override
    public RiskScores compute(FraudMetrics fraudMetrics) {
        double victimRisk = computeVictimRisk(fraudMetrics);
        double fraudsterRisk = computeFraudsterRisk(fraudMetrics);

        log.info("Risk computing for client with id {} is over", fraudMetrics.clientId());
        log.debug("Result for client with id {} : fraudsterRisk={}; victimRisk={}",
                fraudMetrics.clientId(), fraudsterRisk, victimRisk);
        return RiskScores.of(fraudsterRisk, victimRisk);
    }

    private double computeVictimRisk(FraudMetrics fraudMetrics) {
        return (fraudMetrics.weeklyGrowth() > 500 ? 0.4 : 0) +
                (fraudMetrics.newRecipientsCount() > 10 ? 0.3 : 0) +
                (fraudMetrics.avgCheque().compareTo(BigDecimal.valueOf(100_000)) > 0 ? 0.3 : 0);
    }

    private double computeFraudsterRisk(FraudMetrics fraudMetrics) {
        return (fraudMetrics.totalSentToHubs().compareTo(BigDecimal.valueOf(100_000L)) > 0 ? 0.3 : 0) +
                (fraudMetrics.totalSentToHubs().compareTo(BigDecimal.valueOf(200_000L)) > 0 ? 0.2 : 0) +
                (fraudMetrics.txCountToHubs() > 10 ? 0.2 : 0) +
                (fraudMetrics.avgChequeToHubs().compareTo(BigDecimal.valueOf(50_000L)) > 0 ? 0.2 : 0);
    }

}
