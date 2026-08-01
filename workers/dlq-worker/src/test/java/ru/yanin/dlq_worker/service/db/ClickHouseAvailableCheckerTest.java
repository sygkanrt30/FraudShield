package ru.yanin.dlq_worker.service.db;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yanin.dlq_worker.clickhouse.BaseClickhouseTest;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClickHouseAvailableCheckerTest extends BaseClickhouseTest {

    @Autowired
    private ClickHouseAvailableChecker availableChecker;

    @Test
    @Order(1)
    void isAvailable_ShouldReturnTrue() {
        //Act
        boolean result = availableChecker.isAvailable();

        //Assert
        assertThat(result).isTrue();
    }

    @Test
    @Order(2)
    void isAvailable_ShouldReturnFalse_WhenContainerStop() {
        clickhouse.stop();

        //Act
        boolean result = availableChecker.isAvailable();

        //Assert
        assertThat(result).isFalse();
    }

}