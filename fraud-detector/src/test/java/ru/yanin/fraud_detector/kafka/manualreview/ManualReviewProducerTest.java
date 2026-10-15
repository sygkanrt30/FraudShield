package ru.yanin.fraud_detector.kafka.manualreview;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.instancio.Instancio;
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
import ru.yanin.shared.domain.ClientDto;
import ru.yanin.shared.domain.Currency;
import ru.yanin.shared.domain.TransactionEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Vyacheslav Yanin
 */
@SpringBootTest(classes = {ManualReviewProducer.class, KafkaTestConfig.class})
@Testcontainers
@Tag("integration")
@Import(KafkaTestConfig.class)
class ManualReviewProducerTest extends BaseKafkaTest {

    private static final int ASYNC_WAIT_SECONDS = 10;

    @Autowired
    private ManualReviewProducer manualReviewProducer;

    @Autowired
    private DefaultKafkaConsumerFactory<String, TransactionEvent> transactionConsumerFactory;

    private KafkaConsumer<String, TransactionEvent> consumer;

    @BeforeEach
    void setUp() {
        consumer = consumer(transactionConsumerFactory, MANUAL_REVIEW_TOPIC);
    }

    @AfterEach
    void tearDown() {
        if (consumer != null) {
            consumer.close();
        }
    }

    @Test
    void shouldSendTransactionToManualReviewTopic() {
        var event = createTransactionEvent();

        // Act
        manualReviewProducer.sendEvent(event);

        // Assert
        ConsumerRecord<String, TransactionEvent> record = awaitRecord(consumer,
                sent -> event.transactionId().equals(sent.transactionId()));

        assertThat(record.topic()).isEqualTo(MANUAL_REVIEW_TOPIC);
        assertThat(record.value()).isEqualTo(event);
    }

    @Test
    void shouldDelegateSendToConfiguredTopic() {
        KafkaTemplate<String, TransactionEvent> template = mock(KafkaTemplate.class);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        var event = createTransactionEvent();

        try {
            // Act
            producerWith(template, executor).sendEvent(event);

            // Assert
            verify(template, timeout(ASYNC_WAIT_SECONDS * 1000L)).send(MANUAL_REVIEW_TOPIC, event);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void shouldNotBlockCaller_WhenKafkaSendIsSlow() throws InterruptedException {
        var sendStarted = new CountDownLatch(1);
        var releaseSend = new CountDownLatch(1);
        KafkaTemplate<String, TransactionEvent> template = mock(KafkaTemplate.class);
        when(template.send(anyString(), any(TransactionEvent.class))).thenAnswer(invocation -> {
            sendStarted.countDown();
            releaseSend.await(ASYNC_WAIT_SECONDS, TimeUnit.SECONDS);
            return null;
        });
        ExecutorService executor = Executors.newSingleThreadExecutor();
        var producer = producerWith(template, executor);
        var event = createTransactionEvent();

        try {
            // Act
            assertThatCode(() -> producer.sendEvent(event)).doesNotThrowAnyException();

            // Assert
            assertThat(sendStarted.await(ASYNC_WAIT_SECONDS, TimeUnit.SECONDS)).isTrue();
            verify(template, timeout(ASYNC_WAIT_SECONDS * 1000L)).send(MANUAL_REVIEW_TOPIC, event);
        } finally {
            releaseSend.countDown();
            executor.shutdownNow();
        }
    }

    private ManualReviewProducer producerWith(KafkaTemplate<String, TransactionEvent> template,
                                            ExecutorService executor) {
        var producer = new ManualReviewProducer(template, executor);
        ReflectionTestUtils.setField(producer, "topic", MANUAL_REVIEW_TOPIC);
        return producer;
    }

    private TransactionEvent createTransactionEvent() {
        return Instancio.of(TransactionEvent.class)
                .set(field(TransactionEvent::transactionId), UUID.randomUUID())
                .set(field(TransactionEvent::from), createClientDto())
                .set(field(TransactionEvent::to), createClientDto())
                .set(field(TransactionEvent::amount), BigDecimal.valueOf(1000.00))
                .set(field(TransactionEvent::currency), Currency.USD)
                .set(field(TransactionEvent::createdAt), Instant.now())
                .create();
    }

    private ClientDto createClientDto() {
        return Instancio.of(ClientDto.class)
                .set(field(ClientDto::id), UUID.randomUUID())
                .set(field(ClientDto::email), "test@example.com")
                .set(field(ClientDto::fullName), "Test User")
                .create();
    }
}
