package ru.yanin.graph_ingress.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
@Node("Client")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class Client {

    @Id
    @GeneratedValue
    private Long id;

    @Property("clientId")
    private UUID clientId;

    @Property("fullName")
    private String fullName;

    @Relationship(type = "MADE_TRANSACTION", direction = Relationship.Direction.OUTGOING)
    private Set<TransactionRel> transactionsOut = new HashSet<>();

    public static Client of(UUID clientId, String fullName) {
        Client client = new Client();
        client.clientId = clientId;
        client.fullName = fullName;
        return client;
    }
}
