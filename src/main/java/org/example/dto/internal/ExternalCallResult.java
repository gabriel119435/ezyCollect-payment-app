package org.example.dto.internal;

public record ExternalCallResult(
        boolean success,
        String message
) {
}
