package ru.yanin.dlq_worker.kafka.process;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.yanin.shared.domain.TransactionEvent;

import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
@Component
@RequiredArgsConstructor
public class EventProcessor {

    private final ChecksPipeline checksPipeline;
    private final InsertionHandler insertionHandler;

    public void process(TransactionEvent event, Acknowledgment ack, long offset) {
        UUID txId = event.transactionId();
        boolean allChecksSuccess = checksPipeline.flow(txId, ack);
        if (!allChecksSuccess) {
            return;
        }
        insertionHandler.handle(event, ack, offset);
    }
}
