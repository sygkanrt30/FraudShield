package ru.yanin.system_ingress.repo;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.yanin.system_ingress.model.entity.Status;
import ru.yanin.system_ingress.model.entity.TransactionRecord;

import java.util.List;
import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
@Repository
public interface TransactionRecordRepository extends CrudRepository<TransactionRecord, UUID> {

    @Modifying
    @Query("UPDATE TransactionRecord t SET t.status = :status WHERE t.transactionId = :transactionId")
    void updateStatus(UUID transactionId, Status status);

    List<TransactionRecord> findByStatus(Status status);
}
