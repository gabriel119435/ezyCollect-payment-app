package org.example.dto.input;

import java.math.BigDecimal;

public record PaymentInput(
        String firstName,
        String lastName,
        String zipCode,
        String cardInfo,
        BigDecimal paymentValue
) {
}
