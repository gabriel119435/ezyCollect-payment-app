package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.input.BankingDetailsInput;
import org.example.service.BankingDetailsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/banking-details")
public class BankingDetailsController {
    private final BankingDetailsService bankingDetailsService;

    public BankingDetailsController(BankingDetailsService bankingDetailsService) {
        this.bankingDetailsService = bankingDetailsService;
    }

    @PutMapping
    public ResponseEntity<Void> createUser(@RequestBody @Valid BankingDetailsInput input, Authentication authentication) {
        bankingDetailsService.saveBankingDetails(input, authentication);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }
}