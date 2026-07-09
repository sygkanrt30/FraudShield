package ru.yanin.system_ingress.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;
import jakarta.persistence.Id;
import org.springframework.data.domain.Persistable;

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
@Table(name = "client")
public class Client implements Persistable<UUID> {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Transient
    private boolean isNew = true;


    public static Client of(UUID id, String email, String fullName) {
        return new Client(id, email, fullName, true);
    }

    @Override
    public boolean isNew() {
        return this.isNew;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
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
        var client = (Client) o;
        return getId() != null && Objects.equals(getId(), client.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy ?
                proxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
