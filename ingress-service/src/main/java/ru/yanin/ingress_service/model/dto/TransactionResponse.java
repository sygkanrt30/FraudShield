package ru.yanin.ingress_service.model.dto;

import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
public record TransactionResponse(UUID id, String status) {
}
