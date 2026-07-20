package ru.yanin.system_ingress.service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yanin.system_ingress.model.entity.Client;
import ru.yanin.system_ingress.repo.ClientRepository;

import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository repository;

    /**
     * Creates a new client or updates an existing one based on the provided identifier.
     * <p>
     * For existing clients, this method does NOT explicitly call
     * {@code save()}. Instead, it relies on automatic dirty checking
     * within the transactional context - any changes made to the managed entity
     * are automatically flushed to the database at transaction commit time.
     * This means no extra UPDATE query is executed if the provided values match
     * the existing ones.
     * </p>
     *
     * @param clientId the unique identifier of the client
     * @param email    the email address of the client
     * @param fullName the full name of the client
     * @return the persisted client entity
     */
    @Override
    @Transactional
    public Client upsertClient(UUID clientId, String email, String fullName) {
        return repository.findById(clientId)
                .map(existingClient -> {
                    existingClient.setEmail(email);
                    existingClient.setFullName(fullName);
                    return existingClient;
                })
                .orElseGet(() -> repository.save(Client.of(clientId, email, fullName)));
    }
}
