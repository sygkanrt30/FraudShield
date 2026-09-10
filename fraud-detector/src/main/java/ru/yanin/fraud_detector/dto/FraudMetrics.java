package ru.yanin.fraud_detector.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
@Data
@Builder
public class FraudMetrics {

    private UUID clientId;

    private BigDecimal totalSentToHubs;
    private Integer txCountToHubs;
    private BigDecimal avgChequeToHubs;

    // common
    private BigDecimal totalSent;
    private Integer txCount;
    private BigDecimal avgCheque;

    // anomalies
    private Double weeklyGrowth;// (%)
    private Integer newRecipientsCount;

    // metadata
    private Instant metricDate;
    private Instant calculatedAt;
}
