package ru.yanin.fraud_detector.service.neo4j;

import ru.yanin.fraud_detector.dto.RiskScores;
import ru.yanin.fraud_detector.model.clickhouse.FraudMetrics;

import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
public interface Neo4jLabelUpdater {

    void updateLabels(UUID clientId, RiskScores riskScores, FraudMetrics fraudMetrics);
}
