package ru.yanin.fraud_detector.service.clickhouse;


import ru.yanin.fraud_detector.dto.FraudMetricsByClient;
import ru.yanin.fraud_detector.dto.PageRankResult;

import java.util.Set;

/**
 * @author Vyacheslav Yanin
 */
public interface FraudMetricsService {

    FraudMetricsByClient getOrCalculateMetrics(Set<PageRankResult> hubs);
}
