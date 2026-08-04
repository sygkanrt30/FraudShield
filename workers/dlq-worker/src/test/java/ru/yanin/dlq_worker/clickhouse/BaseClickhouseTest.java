package ru.yanin.dlq_worker.clickhouse;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.clickhouse.ClickHouseContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
public abstract class BaseClickhouseTest {

    @Container
    @ServiceConnection
    protected static final ClickHouseContainer clickhouse =
            new ClickHouseContainer(DockerImageName.parse("clickhouse/clickhouse-server:latest"))
                    .withDatabaseName("transactions");

    private static final String INIT_SQL_PATH = "src/test/resources/test-init-clickhouse.sql";


    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @PostConstruct
    void initDatabase() {
        try {
            String sql = new String(Files.readAllBytes(Paths.get(INIT_SQL_PATH)));
            for (String stmt : sql.split(";")) {
                if (!stmt.trim().isEmpty()) {
                    try {
                        jdbcTemplate.execute(stmt.trim());
                    } catch (Exception e) {
                        log.warn("Statement failed: {}", stmt, e);
                    }
                }
            }
            log.info("Database initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize database", e);
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void clear() {
        try {
            jdbcTemplate.execute("TRUNCATE TABLE transactions");
            jdbcTemplate.execute("TRUNCATE TABLE fraud_metrics");
        } catch (Exception e) {
            log.warn("Truncate tables throw exception : {}", e.getMessage());
        }
    }
}
