package ru.yanin.fraud_detector.service.pipeline;

import ru.yanin.shared.domain.TransactionEvent;

/**
 * @author Vyacheslav Yanin
 */
public interface Pipeline {

    void flow(TransactionEvent transaction);
}
