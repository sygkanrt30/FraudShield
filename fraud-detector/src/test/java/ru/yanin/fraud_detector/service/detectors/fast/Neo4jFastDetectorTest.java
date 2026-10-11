package ru.yanin.fraud_detector.service.detectors.fast;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yanin.fraud_detector.exception.TransactionRelationNotFoundException;
import ru.yanin.fraud_detector.model.neo4j.Client;
import ru.yanin.fraud_detector.model.neo4j.TransactionRel;
import ru.yanin.fraud_detector.service.detectors.FraudStatus;
import ru.yanin.fraud_detector.service.neo4j.ClientGraphReader;
import ru.yanin.fraud_detector.service.pipeline.DetectorSolution;
import ru.yanin.shared.domain.ClientDto;
import ru.yanin.shared.domain.Currency;
import ru.yanin.shared.domain.TransactionEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Vyacheslav Yanin
 */
@ExtendWith(MockitoExtension.class)
class Neo4jFastDetectorTest {

    @Mock
    private ClientGraphReader clientGraphReader;

    @InjectMocks
    private Neo4jFastDetector detector;

    @Test
    void shouldReturnLowStatusForBothClients_WhenNobodyIsSuspicious() {
        UUID transactionId = UUID.randomUUID();
        Client from = client(UUID.randomUUID(), false, BigDecimal.valueOf(0.7));
        Client to = client(UUID.randomUUID(), false, BigDecimal.valueOf(0.3));
        from.setTransactionsOut(Set.of(relation(transactionId, to)));
        when(clientGraphReader.getClientById(from.getClientId())).thenReturn(from);

        // Act
        DetectorSolution solution = detector.detect(event(transactionId, from.getClientId(), to.getClientId()));

        // Assert
        assertThat(solution.from().fraudStatus()).isEqualTo(FraudStatus.LOW);
        assertThat(solution.to().fraudStatus()).isEqualTo(FraudStatus.LOW);
    }

    @Test
    void shouldReturnHighStatusForFromClient_WhenSenderIsSuspicious() {
        UUID transactionId = UUID.randomUUID();
        Client from = client(UUID.randomUUID(), true, BigDecimal.valueOf(0.9));
        Client to = client(UUID.randomUUID(), false, BigDecimal.valueOf(0.1));
        from.setTransactionsOut(Set.of(relation(transactionId, to)));
        when(clientGraphReader.getClientById(from.getClientId())).thenReturn(from);

        // Act
        DetectorSolution solution = detector.detect(event(transactionId, from.getClientId(), to.getClientId()));

        // Assert
        assertThat(solution.from().fraudStatus()).isEqualTo(FraudStatus.HIGH);
        assertThat(solution.to().fraudStatus()).isEqualTo(FraudStatus.LOW);
    }

    @Test
    void shouldReturnHighStatusForToClient_WhenRecipientIsSuspicious() {
        UUID transactionId = UUID.randomUUID();
        Client from = client(UUID.randomUUID(), false, BigDecimal.valueOf(0.2));
        Client to = client(UUID.randomUUID(), true, BigDecimal.valueOf(0.8));
        from.setTransactionsOut(Set.of(relation(transactionId, to)));
        when(clientGraphReader.getClientById(from.getClientId())).thenReturn(from);

        // Act
        DetectorSolution solution = detector.detect(event(transactionId, from.getClientId(), to.getClientId()));

        // Assert
        assertThat(solution.from().fraudStatus()).isEqualTo(FraudStatus.LOW);
        assertThat(solution.to().fraudStatus()).isEqualTo(FraudStatus.HIGH);
    }

    @Test
    void shouldReturnOverallRiskOfBothClients() {
        UUID transactionId = UUID.randomUUID();
        Client from = client(UUID.randomUUID(), false, new BigDecimal("0.55"));
        Client to = client(UUID.randomUUID(), false, new BigDecimal("0.45"));
        from.setTransactionsOut(Set.of(relation(transactionId, to)));
        when(clientGraphReader.getClientById(from.getClientId())).thenReturn(from);

        // Act
        DetectorSolution solution = detector.detect(event(transactionId, from.getClientId(), to.getClientId()));

        // Assert
        assertThat(solution.from().overallRisk()).isEqualTo(0.55);
        assertThat(solution.to().overallRisk()).isEqualTo(0.45);
    }

    @Test
    void shouldReturnZeroRisk_WhenOverallRiskIsNull() {
        UUID transactionId = UUID.randomUUID();
        Client from = client(UUID.randomUUID(), false, null);
        Client to = client(UUID.randomUUID(), false, null);
        from.setTransactionsOut(Set.of(relation(transactionId, to)));
        when(clientGraphReader.getClientById(from.getClientId())).thenReturn(from);

        // Act
        DetectorSolution solution = detector.detect(event(transactionId, from.getClientId(), to.getClientId()));

        // Assert
        assertThat(solution.from().overallRisk()).isZero();
        assertThat(solution.to().overallRisk()).isZero();
    }

    @ParameterizedTest
    @CsvSource({"1, true", "2, false", "5, false"})
    void shouldResolveIsNewRecipient(int transactionsToSameRecipient, boolean expectedNewRecipient) {
        UUID transactionId = UUID.randomUUID();
        Client from = client(UUID.randomUUID(), false, BigDecimal.ZERO);
        Client to = client(UUID.randomUUID(), false, BigDecimal.ZERO);

        Set<TransactionRel> relations = new HashSet<>();
        relations.add(relation(transactionId, to));
        for (int i = 1; i < transactionsToSameRecipient; i++) {
            relations.add(relation(UUID.randomUUID(), to));
        }
        from.setTransactionsOut(relations);
        when(clientGraphReader.getClientById(from.getClientId())).thenReturn(from);

        // Act
        DetectorSolution solution = detector.detect(event(transactionId, from.getClientId(), to.getClientId()));

        // Assert
        assertThat(solution.to().newRecipient()).isEqualTo(expectedNewRecipient);
    }

    @Test
    void shouldAlwaysMarkSenderAsNotNewRecipient() {
        UUID transactionId = UUID.randomUUID();
        Client from = client(UUID.randomUUID(), false, BigDecimal.ZERO);
        Client to = client(UUID.randomUUID(), false, BigDecimal.ZERO);
        from.setTransactionsOut(Set.of(relation(transactionId, to)));
        when(clientGraphReader.getClientById(from.getClientId())).thenReturn(from);

        // Act
        DetectorSolution solution = detector.detect(event(transactionId, from.getClientId(), to.getClientId()));

        // Assert
        assertThat(solution.from().newRecipient()).isFalse();
    }

    @Test
    void shouldNotCountTransactionsToOtherRecipients_WhenResolvingIsNewRecipient() {
        UUID transactionId = UUID.randomUUID();
        Client from = client(UUID.randomUUID(), false, BigDecimal.ZERO);
        Client to = client(UUID.randomUUID(), false, BigDecimal.ZERO);
        Client other = client(UUID.randomUUID(), true, BigDecimal.ONE);

        from.setTransactionsOut(new HashSet<>(Set.of(
                relation(transactionId, to),
                relation(UUID.randomUUID(), other),
                relation(UUID.randomUUID(), other))));
        when(clientGraphReader.getClientById(from.getClientId())).thenReturn(from);

        // Act
        DetectorSolution solution = detector.detect(event(transactionId, from.getClientId(), to.getClientId()));

        // Assert
        assertThat(solution.to().newRecipient()).isTrue();
        assertThat(solution.to().fraudStatus()).isEqualTo(FraudStatus.LOW);
    }

    @Test
    void shouldResolveRecipientByTransactionId_WhenSenderHasSeveralRelations() {
        UUID transactionId = UUID.randomUUID();
        Client from = client(UUID.randomUUID(), false, BigDecimal.ZERO);
        Client to = client(UUID.randomUUID(), false, BigDecimal.ZERO);
        Client other = client(UUID.randomUUID(), true, BigDecimal.ONE);

        from.setTransactionsOut(new HashSet<>(Set.of(
                relation(UUID.randomUUID(), other),
                relation(transactionId, to))));
        when(clientGraphReader.getClientById(from.getClientId())).thenReturn(from);

        // Act
        DetectorSolution solution = detector.detect(event(transactionId, from.getClientId(), to.getClientId()));

        // Assert
        assertThat(solution.to().fraudStatus()).isEqualTo(FraudStatus.LOW);
        assertThat(solution.to().newRecipient()).isTrue();
    }

    @Test
    void shouldThrowTransactionRelationNotFound_WhenSenderHasNoRelationWithSuchTransactionId() {
        UUID transactionId = UUID.randomUUID();
        Client from = client(UUID.randomUUID(), false, BigDecimal.ZERO);
        Client to = client(UUID.randomUUID(), false, BigDecimal.ZERO);
        from.setTransactionsOut(Set.of(relation(UUID.randomUUID(), to)));
        when(clientGraphReader.getClientById(from.getClientId())).thenReturn(from);

        // Act
        assertThatThrownBy(() -> detector.detect(event(transactionId, from.getClientId(), to.getClientId())))
                .isInstanceOf(TransactionRelationNotFoundException.class)
                .hasMessage("Transaction " + transactionId
                        + " not found in outgoing transactions of client " + from.getClientId());
    }

    @Test
    void shouldThrowTransactionRelationNotFound_WhenSenderHasNoOutgoingTransactionsAtAll() {
        UUID transactionId = UUID.randomUUID();
        Client from = client(UUID.randomUUID(), false, BigDecimal.ZERO);
        Client to = client(UUID.randomUUID(), false, BigDecimal.ZERO);
        when(clientGraphReader.getClientById(any())).thenReturn(from);

        // Act
        assertThatThrownBy(() -> detector.detect(event(transactionId, from.getClientId(), to.getClientId())))
                .isInstanceOf(TransactionRelationNotFoundException.class)
                .hasMessageContaining("Transaction " + transactionId)
                .hasMessageContaining("client " + from.getClientId());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReadSenderFromGraphReaderByIdFromEvent(boolean isSuspicious) {
        UUID transactionId = UUID.randomUUID();
        Client from = client(UUID.randomUUID(), isSuspicious, BigDecimal.ZERO);
        Client to = client(UUID.randomUUID(), isSuspicious, BigDecimal.ZERO);
        from.setTransactionsOut(Set.of(relation(transactionId, to)));
        when(clientGraphReader.getClientById(from.getClientId())).thenReturn(from);

        // Act
        detector.detect(event(transactionId, from.getClientId(), to.getClientId()));

        // Assert
        verify(clientGraphReader).getClientById(from.getClientId());
    }

    private Client client(UUID clientId, boolean suspicious, BigDecimal overallRisk) {
        Client client = new Client();
        client.setClientId(clientId);
        client.setFullName("Client " + clientId);
        client.setSuspicious(suspicious);
        client.setOverallRisk(overallRisk);
        return client;
    }

    private TransactionRel relation(UUID transactionId, Client target) {
        TransactionRel relation = new TransactionRel();
        relation.setTransactionId(transactionId);
        relation.setAmount(BigDecimal.TEN);
        relation.setCreatedAt(Instant.now().minusSeconds(60));
        relation.setTarget(target);
        return relation;
    }

    private TransactionEvent event(UUID transactionId, UUID fromId, UUID toId) {
        return new TransactionEvent(
                transactionId,
                new ClientDto(fromId, "from-" + fromId + "@test.ru", "Sender"),
                new ClientDto(toId, "to-" + toId + "@test.ru", "Recipient"),
                BigDecimal.valueOf(100),
                Currency.USD,
                Instant.now().minusSeconds(60));
    }
}
