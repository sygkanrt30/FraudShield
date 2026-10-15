package ru.yanin.fraud_detector.kafka.alert;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.yanin.fraud_detector.kafka.BaseKafkaTest;
import ru.yanin.fraud_detector.kafka.KafkaTestConfig;
import ru.yanin.shared.alert.Alert;
import ru.yanin.shared.alert.AlertStatus;
import ru.yanin.shared.alert.AlertType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Vyacheslav Yanin
 */
@SpringBootTest(classes = {AlertProducer.class, KafkaTestConfig.class})
@Testcontainers
@Tag("integration")
@Import(KafkaTestConfig.class)
class AlertProducerTest extends BaseKafkaTest {

    private static final int ASYNC_WAIT_SECONDS = 10;

    @Autowired
    private AlertProducer alertProducer;

    @Autowired
    private DefaultKafkaConsumerFactory<String, Alert> alertConsumerFactory;

    private KafkaConsumer<String, Alert> consumer;

    @BeforeEach
    void setUp() {
        consumer = consumer(alertConsumerFactory, ALERT_TOPIC);
    }

    @AfterEach
    void tearDown() {
        if (consumer != null) {
            consumer.close();
        }
    }

    @Test
    void shouldSendAlertToAlertTopic() {
        var alert = createAlert();

        // Act
        alertProducer.sendEvent(alert);

        // Assert
        ConsumerRecord<String, Alert> record = awaitRecord(consumer,
                sent -> alert.getMetadata().getTxId().equals(sent.getMetadata().getTxId()));

        assertThat(record.topic()).isEqualTo(ALERT_TOPIC);
        assertThat(record.value()).isEqualTo(alert);
    }

    @Test
    void shouldDelegateSendToConfiguredTopic() {
        KafkaTemplate<String, Alert> template = mock(KafkaTemplate.class);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        var alert = createAlert();

        try {
            // Act
            producerWith(template, executor).sendEvent(alert);

            // Assert
            verify(template, timeout(ASYNC_WAIT_SECONDS * 1000L)).send(ALERT_TOPIC, alert);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void shouldNotBlockCaller_WhenKafkaSendIsSlow() throws InterruptedException {
        var sendStarted = new CountDownLatch(1);
        var releaseSend = new CountDownLatch(1);
        KafkaTemplate<String, Alert> template = mock(KafkaTemplate.class);
        when(template.send(anyString(), any(Alert.class))).thenAnswer(invocation -> {
            sendStarted.countDown();
            releaseSend.await(ASYNC_WAIT_SECONDS, TimeUnit.SECONDS);
            return null;
        });
        ExecutorService executor = Executors.newSingleThreadExecutor();
        var producer = producerWith(template, executor);
        var alert = createAlert();

        try {
            // Act
            assertThatCode(() -> producer.sendEvent(alert)).doesNotThrowAnyException();

            // Assert
            assertThat(sendStarted.await(ASYNC_WAIT_SECONDS, TimeUnit.SECONDS)).isTrue();
            verify(template, timeout(ASYNC_WAIT_SECONDS * 1000L)).send(ALERT_TOPIC, alert);
        } finally {
            releaseSend.countDown();
            executor.shutdownNow();
        }
    }

    private AlertProducer producerWith(KafkaTemplate<String, Alert> template, ExecutorService executor) {
        var producer = new AlertProducer(template, executor);
        ReflectionTestUtils.setField(producer, "topic", ALERT_TOPIC);
        return producer;
    }

    private Alert createAlert() {
        return Alert.builder()
                .clientsInfo(Alert.ClientsInfo.builder()
                        .fromClientId(UUID.randomUUID())
                        .toClientId(UUID.randomUUID())
                        .fromClientEmail("sender@test.ru")
                        .toClientEmail("recipient@test.ru")
                        .fromClientName("Sender")
                        .toClientName("Recipient")
                        .build())
                .metadata(Alert.TransactionMetadata.builder()
                        .txId(UUID.randomUUID().toString())
                        .amount(BigDecimal.valueOf(5000))
                        .currency("USD")
                        .timestamp(Instant.now())
                        .build())
                .alertType(AlertType.FRAUD_HIGH_RISK_BLOCKED)
                .status(AlertStatus.HIGH)
                .riskScore(0.85)
                .createdAt(Instant.now())
                .source("FRAUD_DETECTOR")
                .comment("test alert")
                .build();
    }
}
