package ru.yanin.shared.alert;

/**
 * @author Vyacheslav Yanin
 */
public enum AlertType {
    FRAUD_HIGH_RISK_BLOCKED, // overallRisk > 0.6
    FRAUD_CYCLE_DETECTED,
    FRAUD_HUB_TRANSFER,
    FRAUD_ALREADY_SUSPICIOUS,
    FRAUD_MEDIUM_RISK_WARNING, // overallRisk > 0.3 и ≤ 0.6
    FRAUD_UNUSUAL_ACTIVITY,
    FRAUD_NEW_RECIPIENT
}
