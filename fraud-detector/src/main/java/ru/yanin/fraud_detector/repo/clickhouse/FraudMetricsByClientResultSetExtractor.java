package ru.yanin.fraud_detector.repo.clickhouse;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import ru.yanin.fraud_detector.dto.FraudMetricsByClient;

import java.sql.ResultSet;

/**
 * @author Vyacheslav Yanin
 */
public class FraudMetricsByClientResultSetExtractor implements ResultSetExtractor<FraudMetricsByClient> {

    @Override
    public FraudMetricsByClient extractData(ResultSet rs) throws DataAccessException {
        return null;
    }
}
