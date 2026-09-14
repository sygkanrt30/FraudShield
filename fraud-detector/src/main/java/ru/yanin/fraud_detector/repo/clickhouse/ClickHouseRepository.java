package ru.yanin.fraud_detector.repo.clickhouse;

import ru.yanin.fraud_detector.model.clickhouse.FraudMetricsByClient;
import ru.yanin.fraud_detector.model.clickhouse.TransactionStatus;
import ru.yanin.fraud_detector.dto.PageRankResult;
import ru.yanin.shared.domain.ClientDto;
import ru.yanin.shared.domain.TransactionEvent;

import java.util.Optional;
import java.util.Set;

/**
 * @author Vyacheslav Yanin
 */
public interface ClickHouseRepository {

    Optional<FraudMetricsByClient> getMetrics(ClientDto from, ClientDto to, Set<PageRankResult> hubs);

    FraudMetricsByClient calculateAndGetMetrics(ClientDto from, ClientDto to, Set<PageRankResult> hubs);

    void updateStatus(TransactionEvent transaction, TransactionStatus status, boolean isFraud, double riskScore, String fraudReason);
}
