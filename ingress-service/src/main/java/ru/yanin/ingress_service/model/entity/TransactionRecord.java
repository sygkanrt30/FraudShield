package ru.yanin.ingress_service.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.proxy.HibernateProxy;
import jakarta.persistence.Id;
import ru.yanin.ingress_service.model.dto.TransactionRequest;
import ru.yanin.shared.domain.Currency;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Entity
@ToString
@Table(name = "transaction_record")
public class TransactionRecord {
    @Id
    @Column(name = "transaction_id",  nullable = false, unique = true)
    private UUID transactionId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, optional = false)
    @JoinColumn(name = "from_client_id", nullable = false)
    @ToString.Exclude
    private Client from;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, optional = false)
    @JoinColumn(name = "to_client_id", nullable = false)
    @ToString.Exclude
    private Client to;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false)
    private Currency currency;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    public static TransactionRecord ofTransactionRequest(TransactionRequest request) {
        return new TransactionRecord(
                request.transactionId(),
                null,
                null,
                request.amount(),
                Status.PENDING,
                Currency.fromString(request.currency()),
                Instant.now()
        );
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy proxy ?
                proxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy ?
                proxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        var transactionRecord = (TransactionRecord) o;
        return getTransactionId() != null && Objects.equals(getTransactionId(), transactionRecord.getTransactionId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy ?
                proxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
