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

    @Override
    @Transactional
    public Client save(UUID clientId, String email, String fullName) {
        return repository.findById(clientId)
                .orElseGet(() -> repository.save(Client.of(clientId, email, fullName)));
    }
}
