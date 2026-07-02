package ru.yanin.shared.domain;

import ru.yanin.shared.util.Validator;

import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
public record Client(UUID id, String email, String fullName) {

    public Client {
        Validator.validateNonNull(id, email, fullName);
    }
}
