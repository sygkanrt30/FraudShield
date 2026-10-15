package ru.yanin.fraud_detector.service.pipeline;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.KafkaException;
import ru.yanin.fraud_detector.exception.NoHubsException;
import ru.yanin.fraud_detector.exception.PipelineException;
import ru.yanin.fraud_detector.service.alerts.AlertResolver;
import ru.yanin.fraud_detector.service.detectors.Detector;
import ru.yanin.shared.alert.Alert;
import ru.yanin.shared.domain.TransactionEvent;
import ru.yanin.shared.producer.Producer;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Vyacheslav Yanin
 */
@ExtendWith(MockitoExtension.class)
class FraudDetectorPipelineTest {

    @Mock
    private Detector fastDetector;

    @Mock
    private Detector fullDetector;

    @Mock
    private AlertResolver alertResolver;

    @Mock
    private Producer<Alert> alertProducer;

    private FraudDetectorPipeline pipeline;

    private TransactionEvent transaction;

    @BeforeEach
    void setUp() {
        pipeline = new FraudDetectorPipeline(fastDetector, fullDetector, alertResolver, alertProducer);
        transaction = Instancio.of(TransactionEvent.class)
                .set(field(TransactionEvent::amount), BigDecimal.valueOf(1500))
                .set(field(TransactionEvent::createdAt), Instant.now().minusSeconds(60))
                .create();
    }

    @Test
    void shouldSendAlertFromFastDetector_WhenFastDetectorDetectsFraud() {
        var solution = Instancio.create(DetectorSolution.class);
        var alert = Instancio.create(Alert.class);
        when(fastDetector.detect(transaction)).thenReturn(solution);
        when(alertResolver.isAlertNeeded(solution)).thenReturn(true);
        when(alertResolver.resolve(transaction, solution)).thenReturn(alert);

        // Act
        pipeline.flow(transaction);

        // Assert
        verify(alertProducer).sendEvent(alert);
    }

    @Test
    void shouldNotInvokeFullDetector_WhenFastDetectorAlreadyDetectsFraud() {
        var solution = Instancio.create(DetectorSolution.class);
        var alert = Instancio.create(Alert.class);
        when(fastDetector.detect(transaction)).thenReturn(solution);
        when(alertResolver.isAlertNeeded(solution)).thenReturn(true);
        when(alertResolver.resolve(transaction, solution)).thenReturn(alert);

        // Act
        pipeline.flow(transaction);

        // Assert
        verify(fullDetector, never()).detect(any());
    }

    @Test
    void shouldSendAlertFromFullDetector_WhenOnlyFullDetectorDetectsFraud() {
        var fastSolution = Instancio.create(DetectorSolution.class);
        var fullSolution = Instancio.create(DetectorSolution.class);
        var alert = Instancio.create(Alert.class);
        when(fastDetector.detect(transaction)).thenReturn(fastSolution);
        when(fullDetector.detect(transaction)).thenReturn(fullSolution);
        when(alertResolver.isAlertNeeded(fastSolution)).thenReturn(false);
        when(alertResolver.isAlertNeeded(fullSolution)).thenReturn(true);
        when(alertResolver.resolve(transaction, fullSolution)).thenReturn(alert);

        // Act
        pipeline.flow(transaction);

        // Assert
        verify(fullDetector).detect(transaction);
        verify(alertProducer).sendEvent(alert);
    }

    @Test
    void shouldNotSendAlert_WhenNeitherDetectorDetectsFraud() {
        when(fastDetector.detect(transaction)).thenReturn(Instancio.create(DetectorSolution.class));
        when(fullDetector.detect(transaction)).thenReturn(Instancio.create(DetectorSolution.class));
        when(alertResolver.isAlertNeeded(any())).thenReturn(false);

        // Act
        pipeline.flow(transaction);

        // Assert
        verify(alertResolver, never()).resolve(any(), any());
        verifyNoInteractions(alertProducer);
    }

    @Test
    void shouldRethrowNoHubsExceptionFromFastDetectorUnchanged() {
        var noHubs = new NoHubsException("No hub clients found");
        when(fastDetector.detect(transaction)).thenThrow(noHubs);

        // Act & Assert
        assertThatThrownBy(() -> pipeline.flow(transaction)).isSameAs(noHubs);
    }

    @Test
    void shouldRethrowNoHubsExceptionFromFullDetectorUnchanged() {
        var noHubs = new NoHubsException("No hub clients found");
        when(fastDetector.detect(transaction)).thenReturn(Instancio.create(DetectorSolution.class));
        when(alertResolver.isAlertNeeded(any())).thenReturn(false);
        when(fullDetector.detect(transaction)).thenThrow(noHubs);

        // Act & Assert
        assertThatThrownBy(() -> pipeline.flow(transaction)).isSameAs(noHubs);
    }

    @Test
    void shouldWrapUnexpectedDetectorFailureIntoPipelineException() {
        var cause = new IllegalStateException("Neo4j unavailable");
        when(fastDetector.detect(transaction)).thenThrow(cause);

        // Act & Assert
        assertThatThrownBy(() -> pipeline.flow(transaction))
                .isInstanceOf(PipelineException.class)
                .hasMessage(cause.getMessage())
                .hasCause(cause);
    }

    @Test
    void shouldWrapUnexpectedResolverFailureIntoPipelineException() {
        var solution = Instancio.create(DetectorSolution.class);
        var cause = new IllegalArgumentException("risk not computed");
        when(fastDetector.detect(transaction)).thenReturn(solution);
        when(alertResolver.isAlertNeeded(solution)).thenReturn(true);
        when(alertResolver.resolve(transaction, solution)).thenThrow(cause);

        // Act & Assert
        assertThatThrownBy(() -> pipeline.flow(transaction))
                .isInstanceOf(PipelineException.class)
                .hasMessage(cause.getMessage())
                .hasCause(cause);
    }

    @Test
    void shouldWrapAlertProducerFailureIntoPipelineException() {
        var solution = Instancio.create(DetectorSolution.class);
        var alert = Instancio.create(Alert.class);
        var cause = new KafkaException("broker unreachable");
        when(fastDetector.detect(transaction)).thenReturn(solution);
        when(alertResolver.isAlertNeeded(solution)).thenReturn(true);
        when(alertResolver.resolve(transaction, solution)).thenReturn(alert);
        doThrow(cause).when(alertProducer).sendEvent(alert);

        // Act & Assert
        assertThatThrownBy(() -> pipeline.flow(transaction))
                .isInstanceOf(PipelineException.class)
                .hasCause(cause);
    }
}
