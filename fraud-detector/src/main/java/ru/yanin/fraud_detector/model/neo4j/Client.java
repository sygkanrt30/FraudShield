package ru.yanin.fraud_detector.model.neo4j;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

import java.math.BigDecimal;
import java.time.Instant;
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
@Setter
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

    @Property("isWhiteListed")
    private boolean isWhiteListed;

    @Property("isSuspicious")
    private boolean isSuspicious;

    @Property("fraudsterRisk")
    private BigDecimal fraudsterRisk;

    @Property("victimRisk")
    private BigDecimal victimRisk;

    @Property("overallRisk")
    private BigDecimal overallRisk;

    @Property("totalSentToHubs")
    private BigDecimal totalSentToHubs;

    @Property("txCountToHubs")
    private long txCountToHubs;

    @Property("isVictim")
    private boolean isVictim;

    @Property("lastCheckedAt")
    private Instant lastCheckedAt;
}
