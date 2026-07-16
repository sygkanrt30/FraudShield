package ru.yanin.graph_ingress.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yanin.graph_ingress.model.Client;
import ru.yanin.graph_ingress.model.TransactionRel;
import ru.yanin.graph_ingress.repo.GraphTransactionRepository;
import ru.yanin.shared.domain.TransactionEvent;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GraphTransactionPersisterImpl implements GraphTransactionPersister {

    private final GraphTransactionRepository graphTransactionRepository;

    @Override
    @Transactional
    public void persistTransaction(TransactionEvent transaction) {
        Client from = Client.of(transaction.from().id(), transaction.from().fullName());
        Client targetClient = Client.of(transaction.to().id(), transaction.to().fullName());
        this.createAndAddTransactionRel(targetClient, from, transaction);
        graphTransactionRepository.save(from);
        log.debug("Persisting transaction {}", transaction);
    }

    private void createAndAddTransactionRel(Client target, Client from, TransactionEvent transaction) {
        var rel = new TransactionRel();
        rel.setTransactionId(transaction.transactionId());
        rel.setAmount(transaction.amount());
        rel.setCreatedAt(transaction.createdAt());
        rel.setTarget(target);
        from.getTransactionsOut().add(rel);
    }
}
