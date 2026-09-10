package ru.yanin.fraud_detector.service.clickhouse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yanin.fraud_detector.dto.FraudMetricsByClient;
import ru.yanin.fraud_detector.dto.PageRankResult;
import ru.yanin.fraud_detector.repo.clickhouse.ClickHouseRepository;

import java.util.Set;

/**
 * @author Vyacheslav Yanin
 */
@Service
@RequiredArgsConstructor
public class ClickhouseFraudMetricsService implements FraudMetricsService {

    private final ClickHouseRepository clickHouseRepository;

    @Override
    public FraudMetricsByClient getOrCalculateMetrics(Set<PageRankResult> hubs) {
        return clickHouseRepository.getMetrics(hubs)
                .orElse(clickHouseRepository.calculateAndGetMetrics(hubs));
    }
}
