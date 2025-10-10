package org.example.dto.internal.exceptions;

public class BusinessRuleViolationException extends RuntimeException {
    public BusinessRuleViolationException(String message) {
        super(message);
    }
}
