package ru.yanin.graph_ingress.service;


import org.instancio.Instancio;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.neo4j.test.autoconfigure.DataNeo4jTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import ru.yanin.graph_ingress.model.Client;
import ru.yanin.graph_ingress.model.TransactionRel;
import ru.yanin.graph_ingress.neo4j.Neo4jTestConfig;
import ru.yanin.graph_ingress.repo.GraphTransactionRepository;
import ru.yanin.shared.domain.ClientDto;
import ru.yanin.shared.domain.Currency;
import ru.yanin.shared.domain.TransactionEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

/**
 * @author Vyacheslav Yanin
 */
@EnableNeo4jRepositories(basePackages = "ru.yanin.graph_ingress")
@DataNeo4jTest
@Import(GraphTransactionPersisterImpl.class)
class GraphTransactionPersisterImplTest extends Neo4jTestConfig {

    @Autowired
    private GraphTransactionPersisterImpl persister;

    @Autowired
    private GraphTransactionRepository graphTransactionRepository;

    @BeforeEach
    void setUp() {
        graphTransactionRepository.deleteAll();
    }

    @AfterAll
    static void tearDown() {
        Neo4jTestConfig.neo4jContainer.close();
    }

    @Test
    void shouldPersistTransactionSuccessfully() {
        TransactionEvent event = createValidTransactionEvent();

        // Act
        persister.persistTransaction(event);

        // Assert
        Optional<Client> savedFromOpt = graphTransactionRepository.findByClientId(event.from().id());
        assertThat(savedFromOpt).isPresent();

        Client clientFrom = savedFromOpt.get();
        assertThat(clientFrom.getClientId()).isEqualTo(event.from().id());
        assertThat(clientFrom.getFullName()).isEqualTo(event.from().fullName());
        assertThat(clientFrom.getTransactionsOut()).hasSize(1);

        TransactionRel rel = clientFrom.getTransactionsOut().iterator().next();

        assertThat(rel.getTransactionId()).isEqualTo(event.transactionId());
        assertThat(rel.getAmount()).isEqualTo(event.amount());
        assertThat(rel.getCreatedAt()).isEqualTo(event.createdAt());
        assertThat(rel.getTarget().getClientId()).isEqualTo(event.to().id());
    }

    @Test
    void shouldCreateRelationshipToExistingTargetClient() {
        ClientDto existingTo = Instancio.create(ClientDto.class);
        Client targetClient = Client.of(existingTo.id(), existingTo.fullName());
        graphTransactionRepository.save(targetClient);

        TransactionEvent event = createValidEventWithTo(existingTo);

        // Act
        persister.persistTransaction(event);

        // Assert
        Optional<Client> fromClientOpt = graphTransactionRepository.findByClientId(event.from().id());
        assertThat(fromClientOpt).isPresent();

        Client fromClient = fromClientOpt.get();
        assertThat(fromClient.getTransactionsOut()).hasSize(1);

        TransactionRel rel = fromClient.getTransactionsOut().iterator().next();
        assertThat(rel.getTarget().getClientId()).isEqualTo(existingTo.id());
    }

    private TransactionEvent createValidTransactionEvent() {
        return Instancio.of(TransactionEvent.class)
                .generate(field(TransactionEvent::amount),
                        gen -> gen.math().bigDecimal().min(BigDecimal.ONE))
                .generate(field(TransactionEvent::createdAt),
                        gen -> gen.temporal().instant().past())
                .create();
    }

    private TransactionEvent createValidEventWithTo(ClientDto to) {
        ClientDto from = Instancio.create(ClientDto.class);
        return new TransactionEvent(
                UUID.randomUUID(),
                from,
                to,
                BigDecimal.valueOf(100 + Math.random() * 10000),
                Currency.USD,
                Instant.now().minusSeconds(100 + (long)(Math.random()*100000))
        );
    }
}