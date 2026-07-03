package ru.yanin.ingress_service.service;

import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yanin.ingress_service.exception.TransactionIngressException;
import ru.yanin.ingress_service.metrcis.IngressMetrics;
import ru.yanin.ingress_service.model.dto.TransactionRequest;
import ru.yanin.ingress_service.model.dto.event.TransactionEventWithTimer;
import ru.yanin.ingress_service.model.dto.mapper.TransactionEventMapper;
import ru.yanin.ingress_service.service.transaction_record.TransactionRecordService;
import ru.yanin.shared.domain.TransactionEvent;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Service
public class TransactionIngressServiceImpl implements TransactionIngressService {

    private final IngressMetrics ingressMetrics;
    private final ApplicationEventPublisher eventPublisher;
    private final TransactionEventMapper transactionEventMapper;
    private final TransactionRecordService transactionRecordService;

    public TransactionIngressServiceImpl(ApplicationEventPublisher eventPublisher,
                                         IngressMetrics ingressMetrics,
                                         TransactionEventMapper transactionEventMapper,
                                         TransactionRecordService transactionRecordService) {
        this.eventPublisher = eventPublisher;
        this.ingressMetrics = ingressMetrics;
        this.transactionEventMapper = transactionEventMapper;
        this.transactionRecordService = transactionRecordService;
    }

    @Override
    @Transactional
    public void receive(TransactionRequest request) {
        Timer.Sample kafkaTimer = ingressMetrics.startTimer();
        ingressMetrics.recordRequestStart();

        try {
            transactionRecordService.record(request);
            TransactionEvent event = transactionEventMapper.toTransactionEvent(request);
            var eventWithTimer = new TransactionEventWithTimer(event, kafkaTimer);
            eventPublisher.publishEvent(eventWithTimer);
        } catch (Exception e) {
            ingressMetrics.recordRequestEnd(false);
            throw new TransactionIngressException(e.getMessage(), e);
        }
        ingressMetrics.recordRequestEnd(true);
    }
}
