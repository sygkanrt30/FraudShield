package ru.yanin.ingress_service.service.client;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import ru.yanin.ingress_service.model.entity.Client;
import ru.yanin.ingress_service.postgres.BasePostgresTest;
import ru.yanin.ingress_service.repo.ClientRepository;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vyacheslav Yanin
 */
@Tag("integration")
class ClientServiceImplTest extends BasePostgresTest {

    @BeforeAll
    static void waitForContainer() {
        Awaitility.await()
                .atMost(30, TimeUnit.SECONDS)
                .until(postgres::isRunning);
    }

    @Autowired
    private ClientService clientService;

    @Autowired
    private ClientRepository clientRepository;

    @Test
    void shouldSaveNewClient() {
        UUID clientId = UUID.randomUUID();
        String email = "newclient@example.com";
        String fullName = "New Client";

        Client saved = clientService.save(clientId, email, fullName);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo(clientId);
        assertThat(saved.getEmail()).isEqualTo(email);
        assertThat(saved.getFullName()).isEqualTo(fullName);
        assertThat(clientRepository.findById(clientId)).isPresent();
    }

    @Test
    void shouldReturnExistingClient() {
        UUID clientId = UUID.randomUUID();
        String email = "existing@example.com";
        String fullName = "Existing Client";

        clientRepository.save(Client.of(clientId, email, fullName));
        Client result = clientService.save(clientId, "should-not-change@example.com", "Should Not Change");

        assertThat(result.getId()).isEqualTo(clientId);
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getFullName()).isEqualTo(fullName);
        assertThat(clientRepository.count()).isEqualTo(1);
    }

    @RepeatedTest(value = 3, name = "shouldBeIdempotent {currentRepetition} of {totalRepetitions}")
    void shouldBeIdempotent() {
        UUID clientId = UUID.randomUUID();

        Client first = clientService.save(clientId, "test1@example.com", "First Name");
        Client second = clientService.save(clientId, "test2@example.com", "Second Name");

        assertThat(first.getId()).isEqualTo(second.getId());
        assertThat(first.getEmail()).isEqualTo(second.getEmail());
    }
}