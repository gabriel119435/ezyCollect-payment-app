package org.example.config;


import lombok.extern.slf4j.Slf4j;
import org.example.dto.internal.exceptions.BadConfigurationException;
import org.example.dto.internal.exceptions.BusinessRuleViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("validation error", ex);

        List<Map<String, String>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                // order for unit tests
                .sorted(Comparator.comparing(FieldError::getField))
                .map(error -> Map.of(
                        "field", error.getField(),
                        "message", error.getDefaultMessage() != null
                                ? error.getDefaultMessage()
                                : "null error message, check logs"
                ))
                .toList();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("validation error", errors));
    }

    @ExceptionHandler(BadConfigurationException.class)
    public ResponseEntity<?> handleBadConfigurationException(BadConfigurationException ex) {
        log.error("bad config", ex);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("bad config", ex.getMessage()));
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<?> handleBusinessRuleViolationException(BusinessRuleViolationException ex) {
        log.error("bad config", ex);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("bad config", ex.getMessage()));
    }
}