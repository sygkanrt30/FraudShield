package ru.yanin.dlq_worker.service.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClickHouseAvailableChecker implements DBAvailableChecker {

    @Override
    public boolean isAvailable() {
        return false;
    }
}
