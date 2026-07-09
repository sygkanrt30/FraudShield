package ru.yanin.graph_ingress.service;

import ru.yanin.shared.domain.TransactionEvent;

/**
 * @author Vyacheslav Yanin
 */
public interface GraphTransactionPersister {

    void persistTransaction(TransactionEvent transaction);
}
