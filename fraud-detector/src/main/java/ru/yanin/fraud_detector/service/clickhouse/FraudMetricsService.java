package ru.yanin.fraud_detector.service.clickhouse;


import ru.yanin.fraud_detector.model.clickhouse.FraudMetricsByClient;
import ru.yanin.fraud_detector.dto.PageRankResult;
import ru.yanin.shared.domain.ClientDto;

import java.util.Set;

/**
 * @author Vyacheslav Yanin
 */
public interface FraudMetricsService {

    FraudMetricsByClient getOrCalculateMetrics(ClientDto from, ClientDto to, Set<PageRankResult> hubs);
}
