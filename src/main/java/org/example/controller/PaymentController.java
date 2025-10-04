package org.example.controller;

import org.example.dto.input.PaymentInput;
import org.example.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<Void> createPayment(@RequestBody PaymentInput input, Authentication authentication) {
        paymentService.createPayment(input, authentication);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }
}