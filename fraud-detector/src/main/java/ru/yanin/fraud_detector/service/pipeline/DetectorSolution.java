package ru.yanin.fraud_detector.service.pipeline;

import lombok.Builder;
import ru.yanin.fraud_detector.service.detectors.FraudStatus;

/**
 * @author Vyacheslav Yanin
 */
@Builder
public record DetectorSolution(
        ClientSolution from,
        ClientSolution to
) {

    @Builder
    public record ClientSolution(
            FraudStatus fraudStatus,
            double overallRisk,
            boolean newRecipient
    ) {
    }
}