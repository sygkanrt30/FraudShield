package ru.yanin.fraud_detector.service.clickhouse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yanin.fraud_detector.model.clickhouse.FraudMetricsByClient;
import ru.yanin.fraud_detector.dto.PageRankResult;
import ru.yanin.fraud_detector.repo.clickhouse.ClickHouseRepository;
import ru.yanin.shared.domain.ClientDto;

import java.util.Set;

/**
 * @author Vyacheslav Yanin
 */
@Service
@RequiredArgsConstructor
public class ClickhouseFraudMetricsService implements FraudMetricsService {

    private final ClickHouseRepository clickHouseRepository;

    @Override
    public FraudMetricsByClient getOrCalculateMetrics(ClientDto from, ClientDto to, Set<PageRankResult> hubs) {
        return clickHouseRepository.getMetrics(from, to, hubs)
                .orElse(clickHouseRepository.calculateAndGetMetrics(from, to, hubs));
    }
}
