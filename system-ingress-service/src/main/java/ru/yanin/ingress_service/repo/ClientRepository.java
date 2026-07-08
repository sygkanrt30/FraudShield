package ru.yanin.ingress_service.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.yanin.ingress_service.model.entity.Client;

import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
@Repository
public interface ClientRepository extends CrudRepository<Client, UUID> {

}
