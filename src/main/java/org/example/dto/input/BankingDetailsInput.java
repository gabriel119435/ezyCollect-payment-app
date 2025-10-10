package org.example.dto.input;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.example.dto.input.annotation.ValidBodyTemplate;
import org.example.dto.input.annotation.ValidURL;

public record BankingDetailsInput(
        @NotEmpty @Size(max = 50)
        String accountNumber,
        @NotEmpty @Size(max = 50)
        String routingNumber,
        @NotEmpty @Size(max = 100)
        String bankName,
        @NotEmpty @Size(max = 200) @ValidURL
        String webhookUrl,
        @NotEmpty @Size(max = 500) @ValidBodyTemplate
        String bodyTemplate
) {
}
