package ru.yanin.fraud_detector.service.neo4j;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yanin.fraud_detector.exception.ClientNotFoundException;
import ru.yanin.fraud_detector.model.neo4j.Client;
import ru.yanin.fraud_detector.repo.neo4j.ClientNeo4jRepository;

import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClientNeo4jReader implements ClientGraphReader {

    private final ClientNeo4jRepository clientNeo4JRepository;

    @Override
    public Client getClientById(UUID clientId) {
        return clientNeo4JRepository.findClientByClientId(clientId)
                .orElseThrow(() -> new ClientNotFoundException("Client not found by id " + clientId));
    }
}
