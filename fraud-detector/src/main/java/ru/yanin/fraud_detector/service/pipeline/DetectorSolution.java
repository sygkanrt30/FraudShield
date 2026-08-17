package ru.yanin.fraud_detector.service.pipeline;

import lombok.Builder;

/**
 * @author Vyacheslav Yanin
 */
@Builder
public record DetectorSolution(
        FraudStatusClientsContainer statusClientsContainer,
        boolean isCycle,
        double overallRisk,
        boolean newRecipient,
        boolean isHubTransfer
) {
}
