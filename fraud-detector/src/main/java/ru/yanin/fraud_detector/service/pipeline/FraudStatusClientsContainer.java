package ru.yanin.fraud_detector.service.pipeline;

import ru.yanin.fraud_detector.service.detectors.FraudStatus;

/**
 * @author Vyacheslav Yanin
 */
public record FraudStatusClientsContainer(FraudStatus from, FraudStatus to) {
}
