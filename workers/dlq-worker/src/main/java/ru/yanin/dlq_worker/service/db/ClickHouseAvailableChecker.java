package ru.yanin.dlq_worker.service.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * @author Vyacheslav Yanin
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
