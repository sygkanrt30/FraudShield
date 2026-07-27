package ru.yanin.dlq_worker.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yanin.shared.domain.TransactionEvent;
import ru.yanin.shared.producer.Producer;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeadQueueProducer implements Producer<TransactionEvent> {

    @Override
    public void sendMessage(TransactionEvent data) {

    }
}
