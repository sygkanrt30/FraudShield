package ru.yanin.dlq_worker.kafka.process.checks;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.yanin.dlq_worker.kafka.process.checks.util.ProcessingChainFactory;

import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
@Component
@RequiredArgsConstructor
public final class PipelineChecks {

    private final ProcessingContext ctx;

    public boolean flow(UUID txId, Acknowledgment ack) {
        var factory = new ProcessingChainFactory();
        return factory.defaultChain()
                .process(ctx.setTxId(txId).setAck(ack));
    }
}
