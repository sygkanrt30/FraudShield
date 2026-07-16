package ru.yanin.graph_ingress.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
@Setter
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@RelationshipProperties
public class TransactionRel {

    @RelationshipId
    @GeneratedValue
    private Long id;

    @Property("transactionId")
    private UUID transactionId;

    @Property("amount")
    private BigDecimal amount;

    @Property("createdAt")
    private Instant createdAt;

    @TargetNode
    private Client target;
}
