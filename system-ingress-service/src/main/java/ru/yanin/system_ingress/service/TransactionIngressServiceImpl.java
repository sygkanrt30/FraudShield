package ru.yanin.system_ingress.service;

import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yanin.system_ingress.exception.TransactionIngressException;
import ru.yanin.system_ingress.metrcis.IngressMetrics;
import ru.yanin.system_ingress.model.dto.TransactionRequest;
import ru.yanin.system_ingress.model.dto.event.TransactionEventWithTimer;
import ru.yanin.system_ingress.model.dto.mapper.TransactionEventMapper;
import ru.yanin.system_ingress.service.transaction_record.TransactionRecordService;
import ru.yanin.shared.domain.TransactionEvent;

/**
 * Implementation of {@link TransactionIngressService} responsible for processing incoming transaction requests.
 * <p>
 * After successfully persisting the transaction, this service publishes a {@link TransactionEventWithTimer}
 * which is then asynchronously captured and sent to the message broker by {@link ru.yanin.system_ingress.event.EventHandler}
 * in the {@code AFTER_COMMIT} phase.
 * </p>
 *
 * @author Vyacheslav Yanin
 * @see ru.yanin.system_ingress.event.EventHandler
 * @see TransactionEventWithTimer
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionIngressServiceImpl implements TransactionIngressService {

    private final IngressMetrics ingressMetrics;
    private final ApplicationEventPublisher eventPublisher;
    private final TransactionEventMapper transactionEventMapper;
    private final TransactionRecordService transactionRecordService;

    @Override
    @Transactional
    public void receive(TransactionRequest request) {
        Timer.Sample kafkaTimer = ingressMetrics.startTimer();
        ingressMetrics.recordRequestStart();

        try {
            transactionRecordService.record(request);
            TransactionEventWithTimer eventWithTimer = getTransactionEventWithTimer(request, kafkaTimer);
            eventPublisher.publishEvent(eventWithTimer);
        } catch (IllegalArgumentException e) {
            ingressMetrics.recordRequestEnd(false);
            throw e;
        } catch (Exception e) {
            ingressMetrics.recordRequestEnd(false);
            throw new TransactionIngressException(e.getMessage(), e);
        }
        ingressMetrics.recordRequestEnd(true);
    }

    private TransactionEventWithTimer getTransactionEventWithTimer(TransactionRequest request, Timer.Sample kafkaTimer) {
        TransactionEvent event = transactionEventMapper.toTransactionEvent(request);
        return new TransactionEventWithTimer(event, kafkaTimer);
    }
}
