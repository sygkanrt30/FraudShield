package ru.yanin.ingress_service.model.dto.event;

import io.micrometer.core.instrument.Timer;
import ru.yanin.shared.domain.TransactionEvent;

/**
 * @author Vyacheslav Yanin
 */
public record TransactionEventWithTimer(
        TransactionEvent transactionEvent,
        Timer.Sample kafkaTimer
) {
}
