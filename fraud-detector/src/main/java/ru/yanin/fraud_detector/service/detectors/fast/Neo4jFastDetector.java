package ru.yanin.fraud_detector.service.detectors.fast;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yanin.fraud_detector.exception.ClientNotFoundException;
import ru.yanin.fraud_detector.model.neo4j.Client;
import ru.yanin.fraud_detector.model.neo4j.TransactionRel;
import ru.yanin.fraud_detector.service.detectors.Detector;
import ru.yanin.fraud_detector.service.detectors.FraudStatus;
import ru.yanin.fraud_detector.service.neo4j.ClientGraphReader;
import ru.yanin.fraud_detector.service.pipeline.DetectorSolution;
import ru.yanin.shared.domain.TransactionEvent;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Qualifier("Neo4jFastDetector")
public class Neo4jFastDetector implements Detector {

    private final ClientGraphReader clientGraphReader;

    @Override
    public DetectorSolution detect(TransactionEvent transaction) {
        Client from = clientGraphReader.getClientById(transaction.from().id());
        Client to = extratToClient(transaction, from);

        FraudStatus fromClientStatus = getClientFraudStatus(from);
        FraudStatus toClientStatus = getClientFraudStatus(to);

        boolean isNewRecipient = resolveIsNewRecipient(from, to);

        return DetectorSolution.builder()
                .from(DetectorSolution.ClientSolution.builder()
                        .fraudStatus(fromClientStatus)
                        .overallRisk(from.getOverallRisk().doubleValue())
                        .newRecipient(false)
                        .build())
                .to(DetectorSolution.ClientSolution.builder()
                        .fraudStatus(toClientStatus)
                        .overallRisk(to.getOverallRisk().doubleValue())
                        .newRecipient(isNewRecipient)
                        .build())
                .build();
    }

    private @NonNull Client extratToClient(TransactionEvent transaction, Client from) {
        return from.getTransactionsOut().stream()
                .filter(transactionRel ->
                        transactionRel.getTransactionId().equals(transaction.transactionId()))
                .map(TransactionRel::getTarget)
                .findFirst()
                .orElseThrow(() -> new ClientNotFoundException("Client not found with id " + transaction.to().id()));
    }

    private FraudStatus getClientFraudStatus(Client client) {
        if (client.isSuspicious()) {
            return FraudStatus.HIGH;
        }
        return FraudStatus.LOW;
    }

    private boolean resolveIsNewRecipient(Client from, Client to) {
        return from.getTransactionsOut().stream()
                .filter(transactionRel ->
                        transactionRel.getTarget().getClientId().equals(to.getClientId()))
                .toList()
                .size() <= 1;
    }
}
