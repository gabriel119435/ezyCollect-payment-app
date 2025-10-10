package org.example.dto.input;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.example.dto.input.annotation.ValidCardInfo;

import java.math.BigDecimal;

public record PaymentInput(
        @NotNull
        @Size(max = 50)
        String firstName,
        @NotNull
        @Size(max = 50)
        String lastName,
        @NotNull
        @Size(max = 20)
        String zipCode,
        @NotNull @ValidCardInfo
        String cardInfo,
        @Positive
        BigDecimal paymentValue
) {
}
