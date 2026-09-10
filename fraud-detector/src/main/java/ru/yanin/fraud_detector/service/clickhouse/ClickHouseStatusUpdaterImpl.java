package ru.yanin.fraud_detector.service.clickhouse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yanin.fraud_detector.dto.RiskScores;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClickHouseStatusUpdaterImpl implements ClickHouseStatusUpdater {

    @Override
    public void updateStatus(RiskScores riskScores) {

    }
}
