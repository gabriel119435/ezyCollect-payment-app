package org.example.dto.exception;

public class ValidationException extends RuntimeException {
    public ValidationException(String msg) {
        super(msg);
    }
}
