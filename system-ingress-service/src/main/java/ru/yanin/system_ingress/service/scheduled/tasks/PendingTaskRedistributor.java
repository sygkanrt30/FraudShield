package ru.yanin.system_ingress.service.scheduled.tasks;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.yanin.system_ingress.service.transaction_record.TransactionRecordService;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Scheduled component that periodically scans for pending transactions that have exceeded
 * the allowed time threshold and transitions them to an expired status.
 *
 * @author Vyacheslav Yanin
 * @see ru.yanin.system_ingress.model.entity.Status
 */
@Component
@RequiredArgsConstructor
public class PendingTaskRedistributor {

    @Value("${app.event.expired.min.minutes}")
    private int lowerExpirationLimit;
    private final TransactionRecordService transactionRecordService;

    @Scheduled(fixedDelayString = "${app.event.expired.fixedDelay}",
            initialDelayString = "${app.event.expired.initialDelay}",
            timeUnit = TimeUnit.SECONDS)
    public void redistribute() {
        Instant threshold = Instant.now().minus(Duration.ofMinutes(lowerExpirationLimit));
        transactionRecordService.markTransactionExpiredIfNeeded(threshold);
    }
}
