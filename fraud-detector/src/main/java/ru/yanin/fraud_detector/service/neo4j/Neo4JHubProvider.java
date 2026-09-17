package ru.yanin.fraud_detector.service.neo4j;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yanin.fraud_detector.dto.PageRankResult;
import ru.yanin.fraud_detector.repo.neo4j.ClientNeo4jRepository;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Vyacheslav Yanin
 */
@Service
@RequiredArgsConstructor
public class Neo4JHubProvider implements HubProvider {

    @Value("${detection.hub.threshold.lower}")
    private double hubLowerThreshold;
    private final ClientNeo4jRepository clientRepository;

    @Override
    public Set<PageRankResult> getHubs() {
        return clientRepository.getPageRankResults().stream()
                .filter(pageRankResult -> pageRankResult.score() >= hubLowerThreshold)
                .collect(Collectors.toSet());
    }
}
