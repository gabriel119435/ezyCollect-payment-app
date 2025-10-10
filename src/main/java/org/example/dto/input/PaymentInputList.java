package org.example.dto.input;

import jakarta.validation.Valid;

import java.util.List;

public record PaymentInputList(
        @Valid List<PaymentInput> payments
) {
}