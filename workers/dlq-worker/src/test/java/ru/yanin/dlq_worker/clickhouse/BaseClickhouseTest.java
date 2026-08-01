package ru.yanin.dlq_worker.clickhouse;

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
import org.testcontainers.utility.MountableFile;

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

    private static final String INIT_SQL_PATH = "../../../clickhouse/init-clickhouse.sql";

    static {
        clickhouse.start();
        try {
            clickhouse.copyFileToContainer(
                    MountableFile.forHostPath(INIT_SQL_PATH),
                    "/tmp/init.sql"
            );

            var result = clickhouse.execInContainer(
                    "bash", "-c",
                    "cat /tmp/init.sql | clickhouse-client --database=transactions --multiquery"
            );

            if (result.getExitCode() != 0) {
                log.error("Init stderr: {}", result.getStderr());
                log.error("Init stdout: {}", result.getStdout());
                throw new RuntimeException("Init failed: " + result.getStderr());
            }

            log.info("ClickHouse initialized successfully");
        } catch (Exception e) {
            throw new RuntimeException("Failed to init ClickHouse", e);
        }
    }

    @Autowired
    protected JdbcTemplate jdbcTemplate;

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
