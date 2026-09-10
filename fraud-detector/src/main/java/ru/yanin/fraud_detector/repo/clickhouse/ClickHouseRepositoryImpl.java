package ru.yanin.fraud_detector.repo.clickhouse;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yanin.fraud_detector.dto.FraudMetricsByClient;
import ru.yanin.fraud_detector.dto.PageRankResult;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Vyacheslav Yanin
 */
@RequiredArgsConstructor
@Repository
public class ClickHouseRepositoryImpl implements ClickHouseRepository {

    private static final String GET_FRAUD_METRICS_QUERY =
            "SELECT * FROM fraud_metrics WHERE calculatedAt >= now() - INTERVAL '1 HOUR' HOUR AND clientId IN (:hubs)";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ResultSetExtractor<FraudMetricsByClient> resultSetExtractor =
            new FraudMetricsByClientResultSetExtractor();

    @Override
    public Optional<FraudMetricsByClient> getMetrics(Set<PageRankResult> hubs) {
        Set<UUID> hubClientIds = hubs.stream()
                .map(PageRankResult::clientId)
                .collect(Collectors.toSet());
        
        var params = new MapSqlParameterSource().addValue("hubs", hubClientIds);
        return Optional.ofNullable(jdbcTemplate.query(GET_FRAUD_METRICS_QUERY, params, resultSetExtractor));
    }

    @Override
    public FraudMetricsByClient calculateAndGetMetrics(Set<PageRankResult> hubs) {
        return null;
    }
}
