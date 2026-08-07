package ru.yanin.shared.alert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Vyacheslav Yanin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

    private String txId;
    private Long fromClientId;
    private Long toClientId;
    private String fromClientEmail;
    private String toClientEmail;
    private String fromClientName;
    private String toClientName;

    private BigDecimal amount;
    private String currency;
    private Long timestamp;

    private AlertType alertType;
    private AlertStatus status;
    private AlertReason reason;
    private Double riskScore;

    private String source;
    private Long createdAt;
    private String comment;

}
