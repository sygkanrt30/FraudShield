package ru.yanin.fraud_detector.service.alerts;

import ru.yanin.shared.alert.Alert;
import ru.yanin.shared.alert.AlertStatus;
import ru.yanin.shared.alert.AlertType;
import ru.yanin.shared.domain.TransactionEvent;

import java.time.Instant;

/**
 * @author Vyacheslav Yanin
 */
final class AlertFabric {

    static Alert buildAlert(TransactionEvent transaction, AlertType alertType, AlertStatus status,
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
