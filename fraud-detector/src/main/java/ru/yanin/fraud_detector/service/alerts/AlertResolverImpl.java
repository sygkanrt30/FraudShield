package ru.yanin.fraud_detector.service.alerts;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yanin.fraud_detector.service.detectors.FraudStatus;
import ru.yanin.fraud_detector.service.pipeline.DetectorSolution;
import ru.yanin.fraud_detector.service.pipeline.FraudStatusClientsContainer;
import ru.yanin.shared.alert.Alert;
import ru.yanin.shared.domain.TransactionEvent;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Service
public class AlertResolverImpl implements AlertResolver {

    @Override
    public boolean isAlertNeeded(FraudStatusClientsContainer statusClientsContainer) {
        return !(statusClientsContainer.from().equals(FraudStatus.LOW) &&
                statusClientsContainer.to().equals(FraudStatus.LOW));
    }

    @Override
    public Alert resolve(TransactionEvent transaction, DetectorSolution detectorSolution) {
        return null;
    }
}
