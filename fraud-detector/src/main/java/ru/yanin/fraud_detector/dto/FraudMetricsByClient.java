package ru.yanin.fraud_detector.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
@Data
@Accessors(fluent = true)
public final class FraudMetricsByClient {

    private final Map<UUID, FraudMetrics> clients;
    private final int totalClients;
    private final BigDecimal totalAmount;
    private final int totalTransactions;
    private final Instant calculatedAt;

    private CalculationMethod calculationMethod;
    private Set<PageRankResult> hubs;
}
