package ru.yanin.ingress_service.service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yanin.ingress_service.model.entity.Client;
import ru.yanin.ingress_service.repo.ClientRepository;

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
    public Client save(UUID transactionId, String email, String fullName) {
        return repository.findById(transactionId)
                .orElseGet(() -> repository.save(Client.of(transactionId, email, fullName)));
    }
}
