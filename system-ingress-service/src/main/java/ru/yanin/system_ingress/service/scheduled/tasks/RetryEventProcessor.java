package ru.yanin.system_ingress.service.scheduled.tasks;

import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.yanin.system_ingress.metrcis.IngressMetrics;
import ru.yanin.system_ingress.model.dto.event.TransactionEventWithTimer;
import ru.yanin.system_ingress.model.dto.mapper.TransactionEventMapper;
import ru.yanin.system_ingress.model.entity.Status;
import ru.yanin.system_ingress.model.entity.TransactionRecord;
import ru.yanin.system_ingress.service.transaction_record.TransactionRecordService;
import ru.yanin.shared.producer.Producer;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Scheduled processor responsible for retrying failed transaction events.
 * <p>
 *
 * <p>The processor handles two distinct failure scenarios:
 * <ul>
 *   <li><b>KAFKA_ERROR</b> - Transactions that failed to be sent to Kafka due to broker issues,
 *   network problems, or serialization errors. These are retried with exponential backoff
 *   or fixed delay based on configuration.</li>
 *   <li><b>EXPIRED</b> - Transactions that were not processed within the expected time window.
 *   These are retried to ensure eventual delivery, even if the original processing window
 *   has passed.</li>
 * </ul>
 * </p>
 *
 * <p><b>Note:</b> This processor does not implement retry limits or backoff strategies internally.
 * That responsibility falls to the {@link Producer} or downstream components. The processor
 * simply retrieves failed records and forwards them for reprocessing.</p>
 *
 * @author Vyacheslav Yanin
 * @see TransactionRecordService
 * @see Status
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RetryEventProcessor {

    private final TransactionRecordService transactionRecordService;
    private final Producer<TransactionEventWithTimer> producer;
    private final TransactionEventMapper transactionEventMapper;
    private final IngressMetrics metrics;

    @Scheduled(fixedDelayString = "${app.kafka.resend.kafka-error.fixedDelay}",
            initialDelayString = "${app.kafka.resend.kafka-error.initialDelay}",
            timeUnit = TimeUnit.SECONDS)
    public void retryKafkaErrorTransactions() {
        List<TransactionRecord> failed = transactionRecordService.findByStatus(Status.KAFKA_ERROR);
        produceTransactionRecord(failed);
    }

    @Scheduled(fixedDelayString = "${app.kafka.resend.expired.fixedDelay}",
            initialDelayString = "${app.kafka.resend.expired.initialDelay}",
            timeUnit = TimeUnit.SECONDS)
    public void retryExpiredTransactions() {
        List<TransactionRecord> failed = transactionRecordService.findByStatus(Status.EXPIRED);
        produceTransactionRecord(failed);
    }

    private void produceTransactionRecord(List<TransactionRecord> failed) {
        for (TransactionRecord record : failed) {
            Timer.Sample sample = metrics.startTimer();
            TransactionEventWithTimer event = transactionEventMapper.toTransactionEventWithTimer(record, sample);
            producer.sendMessage(event);
        }
    }
}
