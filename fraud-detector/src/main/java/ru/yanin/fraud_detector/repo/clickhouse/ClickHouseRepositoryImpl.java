package ru.yanin.fraud_detector.repo.clickhouse;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yanin.fraud_detector.dto.FraudMetricsByClient;
import ru.yanin.fraud_detector.dto.PageRankResult;
import ru.yanin.shared.domain.ClientDto;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.yanin.fraud_detector.repo.clickhouse.FraudMetricsQuery.CALCULATE_FRAUD_METRICS_QUERY;
import static ru.yanin.fraud_detector.repo.clickhouse.FraudMetricsQuery.GET_FRAUD_METRICS_QUERY;

/**
 * @author Vyacheslav Yanin
 */
@RequiredArgsConstructor
@Repository
public class ClickHouseRepositoryImpl implements ClickHouseRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ResultSetExtractor<FraudMetricsByClient> resultSetExtractor =
            new FraudMetricsByClientResultSetExtractor();

    @Override
    public Optional<FraudMetricsByClient> getMetrics(ClientDto from, ClientDto to, Set<PageRankResult> hubs) {
        Set<UUID> hubClientIds = mapToIdsSet(hubs);
        Set<UUID> clientIds = Set.of(from.id(), to.id());
        return Optional.ofNullable(
                jdbcTemplate.query(GET_FRAUD_METRICS_QUERY.query(), getParams(clientIds, hubClientIds), resultSetExtractor));
    }

    private Set<UUID> mapToIdsSet(Set<PageRankResult> hubs) {
        return hubs.stream()
                .map(PageRankResult::clientId)
                .collect(Collectors.toSet());
    }

    private MapSqlParameterSource getParams(Set<UUID> clientIds, Set<UUID> hubClientIds) {
        return new MapSqlParameterSource()
                .addValue("clientIds", clientIds)
                .addValue("hubs", hubClientIds);
    }

    @Override
    public FraudMetricsByClient calculateAndGetMetrics(ClientDto from, ClientDto to, Set<PageRankResult> hubs) {
        Set<UUID> hubClientIds = mapToIdsSet(hubs);
        Set<UUID> clientIds = Set.of(from.id(), to.id());
        return jdbcTemplate.query(CALCULATE_FRAUD_METRICS_QUERY.query(), getParams(clientIds, hubClientIds), resultSetExtractor);
    }
}
