package ru.yanin.fraud_detector.service.pipeline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yanin.fraud_detector.exception.PipelineException;
import ru.yanin.fraud_detector.service.alerts.AlertResolver;
import ru.yanin.fraud_detector.service.detectors.Detector;
import ru.yanin.shared.alert.Alert;
import ru.yanin.shared.domain.TransactionEvent;
import ru.yanin.shared.producer.Producer;

import java.util.stream.Stream;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FraudDetectorPipeline implements Pipeline {

    @Qualifier("Neo4jFastDetector")
    private final Detector fastDetector;
    @Qualifier("FullDetector")
    private final Detector fullDetector;
    private final AlertResolver alertResolver;
    private final Producer<Alert> alertProducer;

    @Override
    public void flow(TransactionEvent transaction) {

        try {
            Stream.of(fastDetector, fullDetector)
                    .map(detector -> detector.detect(transaction))
                    .filter(alertResolver::isAlertNeeded)
                    .findFirst()
                    .ifPresent(result -> {
                        Alert alert = alertResolver.resolve(transaction, result);
                        alertProducer.sendEvent(alert);
                    });
        } catch (Exception e) {
            throw new PipelineException(e.getMessage(), e);
        }
    }
}
