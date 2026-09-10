package ru.yanin.fraud_detector.service.neo4j;

import ru.yanin.fraud_detector.dto.RiskScores;

/**
 * @author Vyacheslav Yanin
 */
public interface Neo4jLabelUpdater {

    void updateLabels(RiskScores riskScores);
}
