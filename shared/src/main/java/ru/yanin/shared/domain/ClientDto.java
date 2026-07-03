package ru.yanin.shared.domain;

import ru.yanin.shared.util.Validator;

import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
public record ClientDto(UUID id, String email, String fullName) {

    public ClientDto {
        Validator.validateNonNull(id, email, fullName);
    }
}
