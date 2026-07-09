package ru.yanin.system_ingress.model.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
public record TransactionRequest(

        @NotNull(message = "Transaction ID is required")
        UUID transactionId,

        @NotNull(message = "Sender ID is required")
        UUID fromClientId,

        @NotNull(message = "Sender full name is required")
        String fromFullName,

        @NotNull(message = "Sender email is required")
        @Email(message = "Invalid email format")
        String fromEmail,

        @NotNull(message = "Recipient ID is required")
        UUID toClientId,

        @NotNull(message = "Recipient full name is required")
        String toFullName,

        @NotNull(message = "Recipient email is required")
        @Email(message = "Invalid email format")
        String toEmail,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,

        @NotNull(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency must be in ISO 4217 format (e.g., RUB, USD)")
        String currency

) {
    public TransactionRequest {
        if (fromClientId.equals(toClientId)) {
            throw new IllegalArgumentException("Sender and recipient cannot be the same");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }
}
