package ru.yanin.fraud_detector.repo.clickhouse;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import ru.yanin.fraud_detector.model.clickhouse.FraudMetrics;
import ru.yanin.fraud_detector.model.clickhouse.FraudMetricsByClient;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
public class FraudMetricsByClientResultSetExtractor implements ResultSetExtractor<FraudMetricsByClient> {

    @Override
    public FraudMetricsByClient extractData(ResultSet rs) throws DataAccessException, SQLException {
        var clients = new HashMap<UUID, FraudMetrics>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalTransactions = 0;
        Instant calculatedAt = null;

        while (rs.next()) {
            UUID clientId = UUID.fromString(rs.getString("clientId"));
            BigDecimal totalSent = rs.getBigDecimal("totalSent");
            int txCount = rs.getInt("txCount");
            if (calculatedAt == null) {
                calculatedAt = rs.getTimestamp("calculatedAt").toInstant();
            }

            var metrics = FraudMetrics.builder()
                    .clientId(clientId)
                    .totalSentToHubs(rs.getBigDecimal("totalSentToHubs"))
                    .txCountToHubs(rs.getInt("txCountToHubs"))
                    .avgChequeToHubs(rs.getBigDecimal("avgChequeToHubs"))
                    .totalSent(totalSent)
                    .txCount(txCount)
                    .avgCheque(rs.getBigDecimal("avgCheque"))
                    .weeklyGrowth(rs.getDouble("weeklyGrowth"))
                    .newRecipientsCount(rs.getInt("newRecipientsCount"))
                    .metricDate(toInstant(rs.getDate("metricDate")))
                    .calculatedAt(rs.getTimestamp("calculatedAt").toInstant())
                    .build();

            clients.put(clientId, metrics);
            totalAmount = totalAmount.add(totalSent);
            totalTransactions += txCount;
        }

        if (clients.isEmpty()) {
            return null;
        }

        return new FraudMetricsByClient(clients, clients.size(), totalAmount, totalTransactions, calculatedAt);
    }

    private Instant toInstant(Date date) {
        return date.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant();
    }
}