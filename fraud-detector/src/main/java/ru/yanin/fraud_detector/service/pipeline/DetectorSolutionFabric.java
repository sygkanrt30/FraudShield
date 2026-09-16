package ru.yanin.fraud_detector.service.pipeline;

import ru.yanin.fraud_detector.dto.RiskScores;
import ru.yanin.fraud_detector.model.clickhouse.FraudMetrics;
import ru.yanin.fraud_detector.service.detectors.FraudStatus;

import java.util.Map;
import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
public final class DetectorSolutionFabric {

    public static DetectorSolution of(UUID fromId, UUID toId,
                                       Map<UUID, FraudStatus> fraudStatusMap,
                                       Map<UUID, RiskScores> riskScores,
                                       Map<UUID, FraudMetrics> metrics) {

        return DetectorSolution.builder()
                .from(clientSolution(fromId, fraudStatusMap, riskScores, metrics))
                .to(clientSolution(toId, fraudStatusMap, riskScores, metrics))
                .build();
    }

    private static DetectorSolution.ClientSolution clientSolution(UUID clientId,
                                                                  Map<UUID, FraudStatus> fraudStatusMap,
                                                                  Map<UUID, RiskScores> riskScores,
                                                                  Map<UUID, FraudMetrics> metrics) {
        RiskScores clientRisk = riskScores.get(clientId);
        FraudMetrics clientMetrics = metrics.get(clientId);

        return DetectorSolution.ClientSolution.builder()
                .fraudStatus(fraudStatusMap.getOrDefault(clientId, FraudStatus.LOW))
                .overallRisk(clientRisk == null ? 0.0 : clientRisk.overallRisk())
                .newRecipient(clientMetrics != null && clientMetrics.newRecipientsCount() > 0)
                .build();
    }
}