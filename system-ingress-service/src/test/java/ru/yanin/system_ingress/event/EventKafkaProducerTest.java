package ru.yanin.system_ingress.event;

import io.micrometer.core.instrument.Timer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.yanin.shared.domain.ClientDto;
import ru.yanin.shared.domain.Currency;
import ru.yanin.shared.domain.TransactionEvent;
import ru.yanin.system_ingress.kafka.BaseKafkaTest;
import ru.yanin.system_ingress.kafka.KafkaTestConfig;
import ru.yanin.system_ingress.metrcis.IngressMetrics;
import ru.yanin.system_ingress.model.dto.event.TransactionEventWithTimer;
import ru.yanin.system_ingress.model.entity.Status;
import ru.yanin.system_ingress.service.transaction_record.TransactionRecordService;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Vyacheslav Yanin
 */
@SpringBootTest(
        classes = {EventKafkaProducer.class, KafkaTestConfig.class}
)
@Testcontainers
@Tag("integration")
@Import(KafkaTestConfig.class)
class EventKafkaProducerTest extends BaseKafkaTest {

    @Autowired
    private EventKafkaProducer eventKafkaProducer;

    @Autowired
    private DefaultKafkaConsumerFactory<String, TransactionEvent> consumerFactory;

    @MockitoBean
    private TransactionRecordService transactionRecordService;

    @MockitoBean
    private IngressMetrics ingressMetrics;

    private KafkaConsumer<String, TransactionEvent> consumer;

    @BeforeEach
    void setUp() {
        consumer = (KafkaConsumer<String, TransactionEvent>) consumerFactory.createConsumer();
        consumer.subscribe(List.of(TOPIC));
        consumer.poll(Duration.ofMillis(200));
    }

    @AfterEach
    void tearDown() {
        if (consumer != null) {
            consumer.close();
        }
    }

    @Test
    void shouldSendMessageToKafkaSuccessfully() {
        TransactionEvent event = createTransactionEvent();
        Timer.Sample sample = Timer.start();
        TransactionEventWithTimer eventWithTimer = new TransactionEventWithTimer(event, sample);

        // Act
        eventKafkaProducer.sendMessage(eventWithTimer);

        // Assert
        verify(transactionRecordService, timeout(5000))
                .updateStatus(event.transactionId(), Status.SENT_TO_KAFKA);

        verify(ingressMetrics, timeout(5000))
                .incrementKafkaPublishCounter(true);
        verify(ingressMetrics, timeout(5000))
                .stopKafkaTimer(any(Timer.Sample.class));

        ConsumerRecords<String, TransactionEvent> records = consumer.poll(Duration.ofSeconds(5));
        assertThat(records).isNotEmpty();
        assertThat(records.iterator().next().value().transactionId())
                .isEqualTo(event.transactionId());
    }

    @Test
    void shouldUpdateStatusToKafkaErrorOnSendFailure() {
        KafkaTemplate<String, TransactionEvent> failingTemplate = mock(KafkaTemplate.class);
        when(failingTemplate.send(anyString(), any(TransactionEvent.class)))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Kafka error")));

        var failingProducer = new EventKafkaProducer(
                failingTemplate,
                ingressMetrics,
                transactionRecordService,
                TOPIC
        );
        TransactionEvent event = createTransactionEvent();
        Timer.Sample sample = Timer.start();
        TransactionEventWithTimer eventWithTimer = new TransactionEventWithTimer(event, sample);

        // Act
        failingProducer.sendMessage(eventWithTimer);

        // Assert
        verify(transactionRecordService, timeout(5000))
                .updateStatus(event.transactionId(), Status.KAFKA_ERROR);
        verify(ingressMetrics, timeout(5000))
                .incrementKafkaPublishCounter(false);
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