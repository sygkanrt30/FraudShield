package ru.yanin.fraud_detector.service.neo4j;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yanin.fraud_detector.dto.RiskScores;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Neo4jLabelUpdaterImpl implements Neo4jLabelUpdater {

    @Override
    public void updateLabels(RiskScores riskScores) {

    }
}
