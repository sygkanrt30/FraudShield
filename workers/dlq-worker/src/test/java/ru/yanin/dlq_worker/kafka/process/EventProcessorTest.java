package ru.yanin.dlq_worker.kafka.process;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import ru.yanin.shared.domain.ClientDto;
import ru.yanin.shared.domain.Currency;
import ru.yanin.shared.domain.TransactionEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * @author Vyacheslav Yanin
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
class EventProcessorTest {

    @Mock
    private PipelineChecks pipelineChecks;

    @Mock
    private InsertionHandler insertionHandler;

    @InjectMocks
    private EventProcessor eventProcessor;
    private Random random;

    @BeforeEach
    void setup() {
        random = new Random();
    }

    @Test
    void shouldProcessEventSuccessfully() {
        UUID txId = UUID.randomUUID();
        TransactionEvent event = createTransactionEvent(txId);
        Acknowledgment ack = mock(Acknowledgment.class);
        when(pipelineChecks.flow(txId, ack)).thenReturn(true);

        //Act
        eventProcessor.process(event, ack, random.nextLong());

        //Assert
        verify(insertionHandler).handle(eq(event), any(), anyLong());
    }

    @Test
    void shouldNotProcessEventSuccessfully_WhenChecksFailed() {
        UUID txId = UUID.randomUUID();
        TransactionEvent event = createTransactionEvent(txId);
        Acknowledgment ack = mock(Acknowledgment.class);
        when(pipelineChecks.flow(txId, ack)).thenReturn(false);

        //Act
        eventProcessor.process(event, ack, random.nextLong());

        //Assert
        verify(insertionHandler, never()).handle(eq(event), any(), anyLong());
    }

    @Test
    void shouldNotHandleException() {
        UUID txId = UUID.randomUUID();
        TransactionEvent event = createTransactionEvent(txId);
        Acknowledgment ack = mock(Acknowledgment.class);
        when(pipelineChecks.flow(txId, ack)).thenThrow(new RuntimeException());

        //Act & Assert
        Assertions.assertThrows(RuntimeException.class,
                () -> eventProcessor.process(event, ack, random.nextLong()));
    }



    private TransactionEvent createTransactionEvent(UUID id) {
        return new TransactionEvent(
                id,
                mock(ClientDto.class),
                mock(ClientDto.class),
                BigDecimal.TEN,
                Currency.USD,
                Instant.now().minus(45L, ChronoUnit.MINUTES)
        );
    }

}