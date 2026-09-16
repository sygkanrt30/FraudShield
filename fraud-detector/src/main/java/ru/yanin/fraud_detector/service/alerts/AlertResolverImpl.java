package ru.yanin.fraud_detector.service.alerts;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yanin.fraud_detector.service.detectors.FraudStatus;
import ru.yanin.fraud_detector.service.pipeline.DetectorSolution;
import ru.yanin.shared.alert.Alert;
import ru.yanin.shared.alert.AlertReason;
import ru.yanin.shared.alert.AlertStatus;
import ru.yanin.shared.alert.AlertType;
import ru.yanin.shared.domain.TransactionEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Service
public class AlertResolverImpl implements AlertResolver {

    private static final double HIGH_RISK_THRESHOLD = 0.6;
    private static final double MEDIUM_RISK_THRESHOLD = 0.3;

    @Override
    public boolean isAlertNeeded(DetectorSolution detectorSolution) {
        return !(detectorSolution.from().fraudStatus().equals(FraudStatus.LOW) &&
                detectorSolution.to().fraudStatus().equals(FraudStatus.LOW));
    }

    @Override
    public Alert resolve(TransactionEvent transaction, DetectorSolution detectorSolution) {
        double overallRisk = Math.max(
                detectorSolution.from().overallRisk(),
                detectorSolution.to().overallRisk());
        boolean isNewRecipient = detectorSolution.from().newRecipient() ||
                detectorSolution.to().newRecipient();

        List<AlertReason> reasons = getReasons(overallRisk, isNewRecipient);
        AlertType alertType = resolveAlertType(overallRisk, isNewRecipient);
        var status = overallRisk > HIGH_RISK_THRESHOLD ? AlertStatus.HIGH : AlertStatus.MEDIUM;
        String comment = resolveComment(alertType, reasons);

        Alert alert = createAlert(transaction, alertType, status, overallRisk, comment);
        log.debug("Resolved {} alert for transaction {}", alertType, transaction.transactionId());
        return alert;
    }

    private List<AlertReason> getReasons(double overallRisk, boolean isNewRecipient) {
        List<AlertReason> reasons = new ArrayList<>();
        if (overallRisk > HIGH_RISK_THRESHOLD) {
            reasons.add(AlertReason.HIGH_RISK_SCORE);
        } else if (overallRisk > MEDIUM_RISK_THRESHOLD) {
            reasons.add(AlertReason.MEDIUM_RISK_SCORE);
        }
        if (isNewRecipient) {
            reasons.add(AlertReason.NEW_RECIPIENT);
        }
        return reasons;
    }

    private AlertType resolveAlertType(double overallRisk, boolean isNewRecipient) {
        if (overallRisk > HIGH_RISK_THRESHOLD) {
            return AlertType.FRAUD_HIGH_RISK_BLOCKED;
        }
        if (overallRisk > MEDIUM_RISK_THRESHOLD) {
            return AlertType.FRAUD_MEDIUM_RISK_WARNING;
        }
        if (isNewRecipient) {
            return AlertType.FRAUD_NEW_RECIPIENT;
        }
        return AlertType.FRAUD_UNUSUAL_ACTIVITY;
    }

    private String resolveComment(AlertType alertType, List<AlertReason> reasons) {
        return alertType.name() + ":" + reasons.stream()
                .map(Enum::name)
                .reduce((a, b) -> a + "," + b)
                .orElse("");
    }

    private Alert createAlert(TransactionEvent transaction, AlertType alertType, AlertStatus status,
                              double overallRisk, String comment) {
        return Alert.builder()
                .clientsInfo(Alert.ClientsInfo.builder()
                        .fromClientId(transaction.from().id())
                        .toClientId(transaction.to().id())
                        .fromClientEmail(transaction.from().email())
                        .toClientEmail(transaction.to().email())
                        .fromClientName(transaction.from().fullName())
                        .toClientName(transaction.to().fullName())
                        .build())
                .metadata(Alert.TransactionMetadata.builder()
                        .txId(transaction.transactionId().toString())
                        .amount(transaction.amount())
                        .currency(transaction.currency().toString())
                        .timestamp(transaction.createdAt())
                        .build())
                .alertType(alertType)
                .status(status)
                .riskScore(overallRisk)
                .createdAt(Instant.now())
                .source("FRAUD_DETECTOR")
                .comment(comment)
                .build();
    }
}