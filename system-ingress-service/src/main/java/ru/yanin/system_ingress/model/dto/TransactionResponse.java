package ru.yanin.system_ingress.model.dto;

import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
public record TransactionResponse(UUID id, String status) {
}
