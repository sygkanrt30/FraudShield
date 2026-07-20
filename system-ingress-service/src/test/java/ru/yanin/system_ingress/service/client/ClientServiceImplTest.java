package ru.yanin.system_ingress.service.client;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import ru.yanin.system_ingress.model.entity.Client;
import ru.yanin.system_ingress.postgres.BasePostgresTest;
import ru.yanin.system_ingress.repo.ClientRepository;

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
    void shouldUpsertClientNewClient() {
        UUID clientId = UUID.randomUUID();
        String email = "newclient@example.com";
        String fullName = "New Client";

        Client saved = clientService.upsertClient(clientId, email, fullName);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo(clientId);
        assertThat(saved.getEmail()).isEqualTo(email);
        assertThat(saved.getFullName()).isEqualTo(fullName);
        assertThat(clientRepository.findById(clientId)).isPresent();
    }

    @Test
    void shouldReturnExistingClientAndUpdate() {
        UUID clientId = UUID.randomUUID();
        String email = "existing@example.com";
        String fullName = "Existing Client";
        String newEmail = "should-change@example.com";
        String newName = "Should Change";

        clientRepository.save(Client.of(clientId, email, fullName));
        Client result = clientService.upsertClient(clientId, newEmail, newName);

        assertThat(result.getId()).isEqualTo(clientId);
        assertThat(result.getEmail()).isEqualTo(newEmail);
        assertThat(result.getFullName()).isEqualTo(newName);
        assertThat(clientRepository.count()).isEqualTo(1);
    }

    @RepeatedTest(value = 3, name = "shouldBeIdempotent {currentRepetition} of {totalRepetitions}")
    void shouldBeIdempotent() {
        UUID clientId = UUID.randomUUID();
        String test2Email = "test2@example.com";
        String secondName = "Second Name";

        Client first = clientService.upsertClient(clientId, "test1@example.com", "First Name");
        Client second = clientService.upsertClient(clientId, test2Email, secondName);
        Client third = clientService.upsertClient(clientId, test2Email, secondName);

        assertThat(first.getId()).isEqualTo(second.getId());
        assertThat(second.getEmail()).isEqualTo(test2Email);
        assertThat(second.getFullName()).isEqualTo(secondName);

        assertThat(third.getEmail()).isEqualTo(test2Email);
        assertThat(third.getFullName()).isEqualTo(secondName);

        assertThat(clientRepository.count()).isEqualTo(1);
    }
}