package ru.yanin.fraud_detector.repo.clickhouse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author Vyacheslav Yanin
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
enum FraudMetricsQuery {

    GET_FRAUD_METRICS_QUERY("SELECT * FROM fraud_metrics " +
            "WHERE calculatedAt >= now() - INTERVAL '1 HOUR' HOUR " +
            "AND clientId IN (:clientIds)"),

    CALCULATE_FRAUD_METRICS_QUERY("""
            WITH
                daily_metrics AS (
                    SELECT
                        fromClientId AS clientId,
                        toDate(timestamp) AS metricDate,
                        sumIf(amount, toClientId IN (:hubs)) AS totalSentToHubs,
                        countIf(toClientId IN (:hubs)) AS txCountToHubs,
                        avgIf(amount, toClientId IN (:hubs)) AS avgChequeToHubs,
                        sum(amount) AS totalSent,
                        count() AS txCount,
                        avg(amount) AS avgCheque
                    FROM transactions
                    WHERE fromClientId IN (:clientIds)
                    GROUP BY fromClientId, toDate(timestamp)
                ),
                weekly_growth AS (
                    SELECT
                        clientId,
                        metricDate,
                        sum(totalSent) OVER (
                            PARTITION BY clientId
                            ORDER BY metricDate
                            ROWS BETWEEN 6 PRECEDING AND CURRENT ROW
                        ) AS thisWeekSent,
                        sum(totalSent) OVER (
                            PARTITION BY clientId
                            ORDER BY metricDate
                            ROWS BETWEEN 13 PRECEDING AND 7 PRECEDING
                        ) AS prevWeekSent
                    FROM daily_metrics
                ),
                new_recipients AS (
                    SELECT
                        t.fromClientId AS clientId,
                        toDate(t.timestamp) AS metricDate,
                        uniqExact(t.toClientId) AS newRecipientsCount
                    FROM transactions t
                    LEFT ANTI JOIN transactions t2
                        ON t.fromClientId = t2.fromClientId
                        AND t.toClientId = t2.toClientId
                        AND t2.timestamp >= t.timestamp - toIntervalDay(7)
                        AND t2.timestamp < t.timestamp
                    WHERE t.fromClientId IN (:clientIds)
                    GROUP BY t.fromClientId, toDate(t.timestamp)
                )
            SELECT
                dm.clientId,
                dm.metricDate,
                dm.totalSentToHubs,
                dm.txCountToHubs,
                dm.avgChequeToHubs,
                dm.totalSent,
                dm.txCount,
                dm.avgCheque,
                if(wg.prevWeekSent > 0,
                   round((wg.thisWeekSent - wg.prevWeekSent) / wg.prevWeekSent * 100, 2),
                   0.0) AS weeklyGrowth,
                ifNull(nr.newRecipientsCount, 0) AS newRecipientsCount,
                now() AS calculatedAt
            FROM daily_metrics dm
            LEFT JOIN weekly_growth wg
                ON dm.clientId = wg.clientId AND dm.metricDate = wg.metricDate
            LEFT JOIN new_recipients nr
                ON dm.clientId = nr.clientId AND dm.metricDate = nr.metricDate
            """),

    UPDATE_TRANSACTION_STATUS_QUERY("""
            INSERT INTO transactions (
                txId, fromClientId, toClientId, amount,
                currency, timestamp, status, isFraud, riskScore, fraudReason, processedAt
            ) VALUES (
                :txId, :fromClientId, :toClientId, :amount,
                :currency, :timestamp, :status, :isFraud, :riskScore, :fraudReason, now()
            )
            """);

    private final String query;
}
