package ru.yanin.fraud_detector.repo.neo4j;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import ru.yanin.fraud_detector.model.neo4j.Client;

import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
@Repository
public interface TransactionNeo4jRepository extends Neo4jRepository<Client, Long> {

    @Query("RETURN EXISTS { MATCH ()-[r:MADE_TRANSACTION]->() WHERE r.transactionId = $transactionId }")
    boolean existsByTransactionId(UUID transactionId);
}
