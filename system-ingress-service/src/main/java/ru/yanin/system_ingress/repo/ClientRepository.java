package ru.yanin.system_ingress.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.yanin.system_ingress.model.entity.Client;

import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
@Repository
public interface ClientRepository extends CrudRepository<Client, UUID> {

}
