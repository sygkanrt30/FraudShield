package ru.yanin.fraud_detector.repo.clickhouse;

import ru.yanin.fraud_detector.dto.FraudMetricsByClient;
import ru.yanin.fraud_detector.dto.PageRankResult;

import java.util.Optional;
import java.util.Set;

/**
 * @author Vyacheslav Yanin
 */
public interface ClickHouseRepository {

    Optional<FraudMetricsByClient> getMetrics(Set<PageRankResult> hubs);

    FraudMetricsByClient calculateAndGetMetrics(Set<PageRankResult> hubs);
}
