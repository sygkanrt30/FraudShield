package ru.yanin.dlq_worker.service.db;

/**
 * Defines a contract for checking database availability.
 * <p>
 * Implementations of this interface provide a mechanism to verify whether
 * a database is accessible and responsive.

 * @author Vyacheslav Yanin
 * @see ClickHouseAvailableChecker
 */
public interface DBAvailableChecker {

    boolean isAvailable();
}
