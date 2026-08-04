package ru.yanin.dlq_worker.kafka.process;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.yanin.dlq_worker.kafka.process.checks.PipelineChecks;
import ru.yanin.shared.domain.TransactionEvent;

import java.util.UUID;

/**
 * Orchestrates the complete DLQ event processing flow.
 * <p>
 * This component coordinates the two-phase processing of transaction events
 * retrieved from the Dead Letter Queue (DLQ):
 * <ol>
 *   <li><b>Validation Phase:</b> Executes pre-insertion checks via {@link PipelineChecks}
 *       to verify the event can be safely processed</li>
 *   <li><b>Persistence Phase:</b> Handles the actual transaction insertion and
 *       state management via {@link InsertionHandler}</li>
 * </ol>
 *
 * @author Vyacheslav Yanin
 */
@Component
@RequiredArgsConstructor
public class EventProcessor {

    private final PipelineChecks pipelineChecks;
    private final InsertionHandler insertionHandler;

    public void process(TransactionEvent event, Acknowledgment ack, long offset) {
        UUID txId = event.transactionId();
        boolean allChecksSuccess = pipelineChecks.flow(txId, ack);
        if (!allChecksSuccess) {
            return;
        }
        insertionHandler.handle(event, ack, offset);
    }
}
