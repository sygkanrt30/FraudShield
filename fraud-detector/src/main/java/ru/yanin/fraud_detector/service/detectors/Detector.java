package ru.yanin.fraud_detector.service.detectors;

import ru.yanin.fraud_detector.service.pipeline.DetectorSolution;
import ru.yanin.shared.domain.TransactionEvent;

/**
 * @author Vyacheslav Yanin
 */
public interface Detector {

    DetectorSolution detect(TransactionEvent transaction);
}
