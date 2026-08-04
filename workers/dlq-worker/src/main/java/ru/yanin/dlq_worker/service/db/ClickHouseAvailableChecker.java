package ru.yanin.dlq_worker.service.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Checks the availability of the ClickHouse database.
 * <p>
 * This component implements a simple health check by executing a lightweight
 * query against the ClickHouse database.
 * <p>
 * The check is performed with a short timeout (4 seconds) to ensure fast
 * failure detection and prevent blocking in case of database unavailability.
 * <p>
 *
 * @author Vyacheslav Yanin
 * @see DBAvailableChecker
 * @see JdbcTemplate
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClickHouseAvailableChecker implements DBAvailableChecker {

    private final JdbcTemplate jdbc;

    @Override
    public boolean isAvailable() {
        jdbc.setQueryTimeout(4);
        try {
            jdbc.queryForObject("SELECT 1", Integer.class);
        } catch (DataAccessException e) {
            return false;
        }
        return true;
    }
}
