package ru.yanin.dlq_worker.kafka.process;

import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import ru.yanin.dlq_worker.service.metrics.DLQMetrics;
import ru.yanin.dlq_worker.service.state.TransactionStateStorage;
import ru.yanin.dlq_worker.service.transaction.TransactionService;
import ru.yanin.shared.domain.ClientDto;
import ru.yanin.shared.domain.Currency;
import ru.yanin.shared.domain.TransactionEvent;
import ru.yanin.shared.producer.Producer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InsertionHandlerTest {

    @Mock
    private DLQMetrics metrics;

    @Mock
    private TransactionService transactionService;

    @Mock
    private TransactionStateStorage stateStorage;

    @Mock
    private Producer<TransactionEvent> deadQueueProducer;

    @Mock
    private Acknowledgment ack;

    @Mock
    private Timer.Sample sample;

    @InjectMocks
    private InsertionHandler insertionHandler;

    private TransactionEvent event;
    private String stringTxId;
    private long offset;

    @BeforeEach
    void setUp() {
        UUID txId = UUID.randomUUID();
        stringTxId = txId.toString();
        offset = 123L;
        event = new TransactionEvent(
                txId,
                new ClientDto(UUID.randomUUID(), "John", "Doe"),
                new ClientDto(UUID.randomUUID(), "Jane", "Smith"),
                BigDecimal.TEN,
                Currency.USD,
                Instant.now().minusSeconds(60)
        );
    }

    @Test
    void shouldProcessSuccessfully_whenInsertSuccess() {
        when(metrics.startInsertTimer()).thenReturn(sample);
        when(transactionService.insertTransaction(event, offset)).thenReturn(true);

        //Act
        insertionHandler.handle(event, ack, offset);

        //Assert
        verify(metrics).startInsertTimer();
        verify(transactionService).insertTransaction(event, offset);
        verify(metrics).stopInsertTimer(sample);
        verify(stateStorage).markAsProcessed(stringTxId);
        verify(metrics).incrementProcessed();
        verify(deadQueueProducer, never()).sendMessage(any());
        verify(stateStorage, never()).markAsDead(anyString());
        verify(metrics, never()).incrementFailed();
        verify(metrics, never()).incrementDeadQueue();
        verify(ack).acknowledge();
        verify(stateStorage).unlock(stringTxId);
    }

    @Test
    void shouldSendToDeadQueue_whenInsertFails() {
        when(metrics.startInsertTimer()).thenReturn(sample);
        when(transactionService.insertTransaction(event, offset)).thenReturn(false);

        //Act
        insertionHandler.handle(event, ack, offset);

        //Assert
        verify(metrics).startInsertTimer();
        verify(transactionService).insertTransaction(event, offset);
        verify(metrics, never()).stopInsertTimer(sample);
        verify(stateStorage, never()).markAsProcessed(stringTxId);
        verify(metrics, never()).incrementProcessed();
        verify(deadQueueProducer).sendMessage(event);
        verify(stateStorage).markAsDead(stringTxId);
        verify(ack).acknowledge();
        verify(stateStorage).unlock(stringTxId);
    }

    @Test
    void shouldAcknowledgeAndUnlock_whenInsertSuccess() {
        when(metrics.startInsertTimer()).thenReturn(sample);
        when(transactionService.insertTransaction(event, offset)).thenReturn(true);

        //Act
        insertionHandler.handle(event, ack, offset);

        //Assert
        verify(ack).acknowledge();
        verify(stateStorage).unlock(stringTxId);
    }

    @Test
    void shouldAcknowledgeAndUnlock_whenInsertFails() {
        when(metrics.startInsertTimer()).thenReturn(sample);
        when(transactionService.insertTransaction(event, offset)).thenReturn(false);

        //Act
        insertionHandler.handle(event, ack, offset);

        //Assert
        verify(ack).acknowledge();
        verify(stateStorage).unlock(stringTxId);
    }
}