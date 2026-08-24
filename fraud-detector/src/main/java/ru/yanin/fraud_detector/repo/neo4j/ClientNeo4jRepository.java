package ru.yanin.fraud_detector.repo.neo4j;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import ru.yanin.fraud_detector.dto.PageRankResult;
import ru.yanin.fraud_detector.model.neo4j.Client;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
@Repository
public interface ClientNeo4jRepository extends Neo4jRepository<Client, Long> {

    @Query("RETURN EXISTS { MATCH ()-[r:MADE_TRANSACTION]->() WHERE r.transactionId = $transactionId }")
    boolean existsByTransactionId(UUID transactionId);

    Optional<Client> findClientByClientId(UUID clientId);

    @Query("""
        CALL gds.pageRank.stream('transactions-graph', {
            dampingFactor: 0.85,
            maxIterations: 20
        })
        YIELD nodeId, score
        WITH gds.util.asNode(nodeId) AS node, score
        WHERE score > 0.1
        RETURN node.id AS nodeId, score
        """)
    Set<PageRankResult> getPageRankResults();
}
