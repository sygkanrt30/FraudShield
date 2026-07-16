package ru.yanin.graph_ingress.repo;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import ru.yanin.graph_ingress.model.Client;

/**
 * @author Vyacheslav Yanin
 */
@Repository
public interface GraphTransactionRepository extends Neo4jRepository<Client, Long> {
}
