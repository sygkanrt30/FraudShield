package ru.yanin.fraud_detector.service.alerts;

import ru.yanin.fraud_detector.service.pipeline.DetectorSolution;
import ru.yanin.shared.alert.Alert;
import ru.yanin.shared.domain.TransactionEvent;

/**
 * @author Vyacheslav Yanin
 */
public interface AlertResolver {

    boolean isAlertNeeded(DetectorSolution detectorSolution);

    Alert resolve(TransactionEvent transaction, DetectorSolution detectorSolution);
}
