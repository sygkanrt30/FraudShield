package ru.yanin.system_ingress.service.client;

import ru.yanin.system_ingress.model.entity.Client;

import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
public interface ClientService {

    Client upsertClient(UUID transactionId, String email, String fullName);
}
