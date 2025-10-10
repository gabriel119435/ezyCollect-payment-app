package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.input.PaymentInputList;
import org.example.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<Void> createPayment(@RequestBody @Valid PaymentInputList inputList, Authentication authentication) {
        paymentService.createPayments(inputList.payments(), authentication);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }
}