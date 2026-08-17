package ru.yanin.fraud_detector.service.neo4j;

import ru.yanin.fraud_detector.model.neo4j.Client;

import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
public interface ClientGraphReader {

    Client getClientById(UUID clientId);
}
