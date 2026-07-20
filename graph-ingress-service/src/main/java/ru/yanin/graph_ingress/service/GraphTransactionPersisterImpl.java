package ru.yanin.graph_ingress.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yanin.graph_ingress.model.Client;
import ru.yanin.graph_ingress.model.TransactionRel;
import ru.yanin.graph_ingress.repo.GraphTransactionRepository;
import ru.yanin.shared.domain.TransactionEvent;

import java.util.UUID;

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
        Client from = upsertClient(
                transaction.from().id(),
                transaction.from().fullName()
        );

        Client targetClient = upsertClient(
                transaction.to().id(),
                transaction.to().fullName()
        );

        TransactionRel rel = createTransactionRel(targetClient, transaction);
        from.getTransactionsOut().add(rel);
        graphTransactionRepository.save(from);
        log.debug("Persisting transaction {}", transaction);
    }

    /**
     * Creates a new client or updates an existing one with the provided full name.
     * <p>
     * If the client already exists, only the {@code fullName} is updated if it differs
     * from the current value. The changes are automatically persisted via dirty checking mechanism.
     * </p>
     *
     * @return the client entity in managed state (existing or newly created)
     */
    private Client upsertClient(UUID clientId, String fullName) {
        return graphTransactionRepository.findByClientId(clientId)
                .map(existingClient -> {
                    if (!existingClient.getFullName().equals(fullName)) {
                        existingClient.setFullName(fullName);
                        log.debug("Updated client {} fullName to '{}'", clientId, fullName);
                    }
                    return existingClient;
                })
                .orElseGet(() -> {
                    Client newClient = Client.of(clientId, fullName);
                    log.debug("Created new client {} with fullName '{}'", clientId, fullName);
                    return newClient;
                });
    }

    private TransactionRel createTransactionRel(Client target, TransactionEvent transaction) {
        var rel = new TransactionRel();
        rel.setTransactionId(transaction.transactionId());
        rel.setAmount(transaction.amount());
        rel.setCreatedAt(transaction.createdAt());
        rel.setTarget(target);
        return rel;
    }
}
