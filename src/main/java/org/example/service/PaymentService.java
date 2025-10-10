package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.input.PaymentInput;
import org.example.dto.internal.exceptions.BusinessRuleViolationException;
import org.example.entity.Payment;
import org.example.entity.User;
import org.example.repository.BankingDetailsRepository;
import org.example.repository.PaymentRepository;
import org.example.repository.PaymentStatusRepository;
import org.example.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class PaymentService {


    private final UserRepository userRepository;
    private final BankingDetailsRepository bankingDetailsRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentStatusRepository paymentStatusRepository;
    private final CryptoService cryptoService;

    public PaymentService(
            UserRepository userRepository,
            BankingDetailsRepository bankingDetailsRepository,
            PaymentRepository paymentRepository,
            PaymentStatusRepository paymentStatusRepository,
            CryptoService cryptoService
    ) {
        this.userRepository = userRepository;
        this.bankingDetailsRepository = bankingDetailsRepository;
        this.paymentRepository = paymentRepository;
        this.paymentStatusRepository = paymentStatusRepository;
        this.cryptoService = cryptoService;
    }

    @Transactional
    public void createPayments(List<PaymentInput> inputs, Authentication authentication) {
        User currentUser = userRepository.findByUsername(authentication.getName()).orElseThrow();

        if (bankingDetailsRepository.findByUser(currentUser).isEmpty())
            throw new BusinessRuleViolationException("user " + currentUser.getUsername() + " has no banking details, so it cannot receive payments");

        inputs.forEach(input -> {
            PaymentInput cryptoInput = new PaymentInput(
                    input.firstName(),
                    input.lastName(),
                    input.zipCode(),
                    cryptoService.encrypt(input.cardInfo()),
                    input.paymentValue()
            );

            List<Payment> paymentsInLastMinute = paymentRepository.findByUserAndCreatedAtAfter(
                    currentUser,
                    Instant.now().minusSeconds(60)
            );

            boolean hasSamePaymentInLastMinute = paymentsInLastMinute
                    .stream().anyMatch(p -> p.isSamePayment(cryptoInput));

            if (hasSamePaymentInLastMinute)
                throw new BusinessRuleViolationException("wait at least 1min to make identical payment");

            Payment payment = paymentRepository.save(new Payment(currentUser, cryptoInput));
            paymentStatusRepository.create(payment.getId());
            log.info("payment created for user {}", currentUser.getUsername());
        });
    }

}
