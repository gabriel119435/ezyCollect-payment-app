package org.example.dto.input;

public record BankingDetailsInput(
        String accountNumber,
        String routingNumber,
        String bankName,
        String webhookUrl,
        String bodyTemplate
) {
}
