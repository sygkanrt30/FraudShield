package ru.yanin.fraud_detector.repo.clickhouse;

import ru.yanin.fraud_detector.dto.FraudMetricsByClient;
import ru.yanin.fraud_detector.dto.PageRankResult;
import ru.yanin.shared.domain.ClientDto;

import java.util.Optional;
import java.util.Set;

/**
 * @author Vyacheslav Yanin
 */
public interface ClickHouseRepository {

    Optional<FraudMetricsByClient> getMetrics(ClientDto from, ClientDto to, Set<PageRankResult> hubs);

    FraudMetricsByClient calculateAndGetMetrics(ClientDto from, ClientDto to, Set<PageRankResult> hubs);
}
