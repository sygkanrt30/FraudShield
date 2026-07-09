package ru.yanin.graph_ingress.service;

import org.springframework.stereotype.Service;
import ru.yanin.shared.domain.TransactionEvent;

/**
 * @author Vyacheslav Yanin
 */
@Service
public class GraphTransactionPersisterImpl implements GraphTransactionPersister {

    @Override
    public void persistTransaction(TransactionEvent transaction) {

    }
}
