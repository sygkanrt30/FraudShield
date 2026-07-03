package ru.yanin.ingress_service.service.client;

import ru.yanin.ingress_service.model.entity.Client;

import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
public interface ClientService {

    Client save(UUID transactionId, String email, String fullName);
}
