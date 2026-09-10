package ru.yanin.fraud_detector.service.detectors;

import java.util.Arrays;

/**
 * @author Vyacheslav Yanin
 */
public enum FraudStatus {
    LOW,
    MEDIUM,
    HIGH;

    public boolean in(FraudStatus... statuses) {
        return Arrays.asList(statuses).contains(this);
    }
}
