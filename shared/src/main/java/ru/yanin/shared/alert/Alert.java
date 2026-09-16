package ru.yanin.shared.alert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

    private ClientsInfo clientsInfo;
    private TransactionMetadata metadata;

    private AlertType alertType;
    private AlertStatus status;
    private double riskScore;
    private Instant createdAt;
    private String source;
    private String comment;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClientsInfo {

        private UUID fromClientId;
        private UUID toClientId;
        private String fromClientEmail;
        private String toClientEmail;
        private String fromClientName;
        private String toClientName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TransactionMetadata {

        private String txId;
        private BigDecimal amount;
        private String currency;
        private Instant timestamp;
    }
}