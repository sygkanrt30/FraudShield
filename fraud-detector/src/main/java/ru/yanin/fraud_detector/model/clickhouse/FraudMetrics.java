package ru.yanin.fraud_detector.model.clickhouse;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
@Data
@Accessors(fluent = true)
@Builder
public class FraudMetrics {

    private UUID clientId;

    private BigDecimal totalSentToHubs;
    private int txCountToHubs;
    private BigDecimal avgChequeToHubs;

    // common
    private BigDecimal totalSent;
    private int txCount;
    private BigDecimal avgCheque;

    // anomalies
    private double weeklyGrowth;// (%)
    private int newRecipientsCount;

    private Instant metricDate;
    private Instant calculatedAt;
}
