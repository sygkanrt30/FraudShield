package ru.yanin.fraud_detector.service.detectors.full;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yanin.fraud_detector.dto.FraudMetricsByClient;
import ru.yanin.fraud_detector.dto.PageRankResult;
import ru.yanin.fraud_detector.dto.RiskScores;
import ru.yanin.fraud_detector.service.clickhouse.ClickHouseStatusUpdater;
import ru.yanin.fraud_detector.service.clickhouse.FraudMetricsService;
import ru.yanin.fraud_detector.service.detectors.Detector;
import ru.yanin.fraud_detector.service.neo4j.HubProvider;
import ru.yanin.fraud_detector.service.neo4j.Neo4jLabelUpdater;
import ru.yanin.fraud_detector.service.pipeline.DetectorSolution;
import ru.yanin.fraud_detector.service.risk.RiskCalculator;
import ru.yanin.shared.domain.TransactionEvent;

import java.util.Set;

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


    @Override
    @Transactional
    public DetectorSolution detect(TransactionEvent transaction) {
        Set<PageRankResult> hubs = hubProvider.getHubs();
        log.trace("Numbers of hubs = {}", hubs.size());

        FraudMetricsByClient metricsByClient = fraudMetricsService.getOrCalculateMetrics(hubs);
        RiskScores riskScores = riskCalculator.compute(metricsByClient);

        labelUpdater.updateLabels(riskScores);
        log.debug("Neo4j labels updated successfully");

        clickhouseUpdater.updateStatus(riskScores);

        return null; //create DetectorSolution
    }
}
