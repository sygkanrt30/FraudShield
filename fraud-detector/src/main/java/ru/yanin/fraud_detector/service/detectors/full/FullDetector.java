package ru.yanin.fraud_detector.service.detectors.full;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yanin.fraud_detector.dto.PageRankResult;
import ru.yanin.fraud_detector.dto.RiskScores;
import ru.yanin.fraud_detector.model.clickhouse.FraudMetrics;
import ru.yanin.fraud_detector.model.clickhouse.FraudMetricsByClient;
import ru.yanin.fraud_detector.service.clickhouse.ClickHouseStatusUpdater;
import ru.yanin.fraud_detector.service.clickhouse.FraudMetricsService;
import ru.yanin.fraud_detector.service.detectors.Detector;
import ru.yanin.fraud_detector.service.detectors.FraudStatus;
import ru.yanin.fraud_detector.service.neo4j.HubProvider;
import ru.yanin.fraud_detector.service.neo4j.Neo4jLabelUpdater;
import ru.yanin.fraud_detector.service.pipeline.DetectorSolution;
import ru.yanin.fraud_detector.service.pipeline.DetectorSolutionFabric;
import ru.yanin.fraud_detector.service.risk.RiskCalculator;
import ru.yanin.shared.domain.TransactionEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Qualifier("FullDetector")
public class FullDetector implements Detector {

    private final HubProvider hubProvider;
    private final FraudMetricsService fraudMetricsService;
    private final RiskCalculator riskCalculator;
    private final Neo4jLabelUpdater labelUpdater;
    private final ClickHouseStatusUpdater clickhouseUpdater;

    private final Map<UUID, FraudStatus> fraudStatusMap = new HashMap<>();
    private final Map<UUID, RiskScores> riskScoresByClient = new HashMap<>();

    @Override
    @Transactional
    public DetectorSolution detect(TransactionEvent transaction) {
        Set<PageRankResult> hubs = hubProvider.getHubs();
        log.trace("Numbers of hubs = {}", hubs.size());

        FraudMetricsByClient metricsByClient = fraudMetricsService
                .getOrCalculateMetrics(transaction.from(), transaction.to(),hubs);

        resetState();

        for (Map.Entry<UUID, FraudMetrics> entry : metricsByClient.clients().entrySet()) {
            RiskScores riskScores = riskCalculator.compute(entry.getValue());

            labelUpdater.updateLabels(entry.getKey(), riskScores, entry.getValue());
            log.debug("Neo4j labels updated successfully");

            clickhouseUpdater.updateStatus(transaction, riskScores);

            riskScoresByClient.put(entry.getKey(), riskScores);
            putInFraudStatusMap(entry, riskScores);
        }

        return DetectorSolutionFabric.build(
                transaction.from().id(),
                transaction.to().id(),
                fraudStatusMap, riskScoresByClient,
                metricsByClient.clients());
    }

    private void resetState() {
        fraudStatusMap.clear();
        riskScoresByClient.clear();
    }

    private void putInFraudStatusMap(Map.Entry<UUID, FraudMetrics> entry, RiskScores riskScores) {
        var status = switch (riskScores.riskCategory()) {
            case FRAUDSTER -> FraudStatus.HIGH;
            case VICTIM, MIXED -> FraudStatus.MEDIUM;
            case SAFE -> FraudStatus.LOW;
        };
        fraudStatusMap.put(entry.getKey(), status);
    }
}
