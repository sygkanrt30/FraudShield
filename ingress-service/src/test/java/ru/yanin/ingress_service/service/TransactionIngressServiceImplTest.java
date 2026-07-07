package ru.yanin.ingress_service.service;

import io.micrometer.core.instrument.Timer;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import ru.yanin.ingress_service.exception.TransactionIngressException;
import ru.yanin.ingress_service.metrcis.IngressMetrics;
import ru.yanin.ingress_service.model.dto.TransactionRequest;
import ru.yanin.ingress_service.model.dto.event.TransactionEventWithTimer;
import ru.yanin.ingress_service.model.dto.mapper.TransactionEventMapper;
import ru.yanin.ingress_service.service.transaction_record.TransactionRecordService;
import ru.yanin.shared.domain.ClientDto;
import ru.yanin.shared.domain.Currency;
import ru.yanin.shared.domain.TransactionEvent;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Vyacheslav Yanin
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
class TransactionIngressServiceImplTest {

    @Mock
    private IngressMetrics ingressMetrics;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private TransactionEventMapper transactionEventMapper;

    @Mock
    private TransactionRecordService transactionRecordService;

    @InjectMocks
    private TransactionIngressServiceImpl service;

    private TransactionRequest request;

    @BeforeEach
    void setUp() {
        request = createValidTransactionRequest();
    }

    @Test
    void receive_shouldProcessSuccessfully() {
        var eventCaptor = ArgumentCaptor.forClass(TransactionEventWithTimer.class);
        Timer.Sample timerSample = mock(Timer.Sample.class);
        TransactionEvent event = createValidTransactionEvent();
        when(ingressMetrics.startTimer()).thenReturn(timerSample);
        when(transactionEventMapper.toTransactionEvent(request)).thenReturn(event);

        service.receive(request);

        verify(transactionRecordService).record(request);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        verify(ingressMetrics).recordRequestEnd(true);

        TransactionEventWithTimer captured = eventCaptor.getValue();
        assertThat(captured.transactionEvent()).isEqualTo(event);
        assertThat(captured.kafkaTimer()).isSameAs(timerSample);
    }

    @Test
    void receive_shouldHandleExceptionAndRecordFailure() {
        Timer.Sample timerSample = mock(Timer.Sample.class);
        RuntimeException dbException = new RuntimeException("Database error");

        when(ingressMetrics.startTimer()).thenReturn(timerSample);
        doThrow(dbException).when(transactionRecordService).record(request);

        assertThatThrownBy(() -> service.receive(request))
                .isInstanceOf(TransactionIngressException.class)
                .hasCause(dbException);

        verify(transactionRecordService).record(request);
        verify(applicationEventPublisher, never()).publishEvent(any());
        verify(ingressMetrics).recordRequestEnd(false);
    }

    @Test
    void receive_shouldHandleMapperException() {
        Timer.Sample timerSample = mock(Timer.Sample.class);
        var mapperException = new RuntimeException("Mapping failed");

        when(ingressMetrics.startTimer()).thenReturn(timerSample);
        when(transactionEventMapper.toTransactionEvent(request)).thenThrow(mapperException);

        assertThatThrownBy(() -> service.receive(request))
                .isInstanceOf(TransactionIngressException.class)
                .hasCause(mapperException);

        verify(transactionRecordService).record(request);
        verify(ingressMetrics).recordRequestEnd(false);
    }

    private TransactionRequest createValidTransactionRequest() {
        return Instancio.of(TransactionRequest.class)
                .set(field(TransactionRequest::fromClientId), UUID.randomUUID())
                .set(field(TransactionRequest::toClientId), UUID.randomUUID())
                .set(field(TransactionRequest::amount), BigDecimal.valueOf(250.00))
                .set(field(TransactionRequest::currency), "USD")
                .set(field(TransactionRequest::fromEmail), "ivan@example.com")
                .set(field(TransactionRequest::toEmail), "petr@example.com")
                .set(field(TransactionRequest::fromFullName), "Ivan Ivanov")
                .set(field(TransactionRequest::toFullName), "Petr Petrov")
                .create();
    }

    private TransactionEvent createValidTransactionEvent() {
        return Instancio.of(TransactionEvent.class)
                .set(field(TransactionEvent::amount), BigDecimal.valueOf(250.00))
                .set(field(TransactionEvent::currency), Currency.USD)
                .set(field("from"), new ClientDto(
                        UUID.randomUUID(),
                        "ivan@example.com",
                        "Ivan Ivanov"
                ))
                .set(field("to"), new ClientDto(
                        UUID.randomUUID(),
                        "petr@example.com",
                        "Petr Petrov"
                ))
                .create();
    }
}