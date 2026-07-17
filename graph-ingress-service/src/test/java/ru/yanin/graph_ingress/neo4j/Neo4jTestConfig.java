package ru.yanin.graph_ingress.neo4j;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * @author Vyacheslav Yanin
 */
@Testcontainers
public abstract class Neo4jTestConfig {

    @Container
    @ServiceConnection
    public static final Neo4jContainer<?> neo4jContainer =
            new Neo4jContainer<>("neo4j:5.26.0-community")
                    .withAdminPassword("password123");
}
