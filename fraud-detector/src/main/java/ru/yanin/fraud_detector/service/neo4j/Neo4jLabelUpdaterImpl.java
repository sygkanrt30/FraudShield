package ru.yanin.fraud_detector.service.neo4j;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yanin.fraud_detector.dto.RiskScores;
import ru.yanin.fraud_detector.exception.ClientNotFoundException;
import ru.yanin.fraud_detector.model.clickhouse.FraudMetrics;
import ru.yanin.fraud_detector.model.neo4j.Client;
import ru.yanin.fraud_detector.repo.neo4j.ClientNeo4jRepository;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Neo4jLabelUpdaterImpl implements Neo4jLabelUpdater {

    private final ClientNeo4jRepository neo4jRepository;

    @Override
    @Transactional
    public void updateLabels(UUID clientId, RiskScores riskScores, FraudMetrics fraudMetrics) {
        Client client = neo4jRepository.findClientByClientId(clientId).orElseThrow(ClientNotFoundException::new);
        updateClient(client, riskScores, fraudMetrics);
        neo4jRepository.save(client);
        log.debug("Client's ({}) labels have been successfully updated", clientId);
    }

    private void updateClient(Client client, RiskScores riskScores, FraudMetrics fraudMetrics) {
        client.setSuspicious(riskScores.overallRisk() > 0.6);
        client.setVictim(riskScores.victimRisk() > 0.7);

        client.setFraudsterRisk(new BigDecimal(riskScores.fraudsterRisk()));
        client.setVictimRisk(new BigDecimal(riskScores.victimRisk()));
        client.setOverallRisk(new BigDecimal(riskScores.overallRisk()));

        client.setTotalSentToHubs(fraudMetrics.totalSentToHubs());
        client.setTxCountToHubs(fraudMetrics.txCountToHubs());
        client.setLastCheckedAt(fraudMetrics.calculatedAt());
    }
}
