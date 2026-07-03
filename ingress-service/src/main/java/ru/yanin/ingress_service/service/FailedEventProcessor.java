package ru.yanin.ingress_service.service;

import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.yanin.ingress_service.metrcis.IngressMetrics;
import ru.yanin.ingress_service.model.dto.event.TransactionEventWithTimer;
import ru.yanin.ingress_service.model.dto.mapper.TransactionEventMapper;
import ru.yanin.ingress_service.model.entity.Status;
import ru.yanin.ingress_service.model.entity.TransactionRecord;
import ru.yanin.ingress_service.service.transaction_record.TransactionRecordService;
import ru.yanin.shared.producer.Producer;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Vyacheslav Yanin
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FailedEventProcessor {

    private final TransactionRecordService transactionRecordService;
    private final Producer<TransactionEventWithTimer> producer;
    private final TransactionEventMapper transactionEventMapper;
    private final IngressMetrics metrics;

    @Scheduled(fixedDelayString = "${app.kafka.resend.fixedDelay}",
            initialDelayString = "${app.kafka.resend.initialDelay}",
            timeUnit = TimeUnit.SECONDS)
    public void retryFailedTransactions() {
        List<TransactionRecord> failed = transactionRecordService.findByStatus(Status.KAFKA_ERROR);
        for (TransactionRecord record : failed) {
            Timer.Sample sample = metrics.startTimer();
            TransactionEventWithTimer event = transactionEventMapper.toTransactionEventWithTimer(record, sample);
            producer.sendMessage(event);
        }
    }
}
