package org.example.service;


import org.example.BaseMockTest;
import org.example.dto.input.PaymentInput;
import org.example.dto.internal.exceptions.BusinessRuleViolationException;
import org.example.entity.BankingDetails;
import org.example.entity.Payment;
import org.example.entity.User;
import org.example.repository.BankingDetailsRepository;
import org.example.repository.PaymentRepository;
import org.example.repository.PaymentStatusRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PaymentServiceTest extends BaseMockTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BankingDetailsRepository bankingDetailsRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentStatusRepository paymentStatusRepository;
    @Mock
    private CryptoService cryptoService;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private PaymentService paymentService;

    @Test
    void createPaymentsSuccess() {
        User user = new User("user1", "pass");
        PaymentInput input = new PaymentInput("john", "doe", "12345", "cardInfo", BigDecimal.TEN);
        Payment payment = new Payment(user, input);
        payment.setId(1L);

        when(authentication.getName()).thenReturn("user1");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(bankingDetailsRepository.findByUser(user)).thenReturn(Optional.of(mock(BankingDetails.class)));
        when(cryptoService.encrypt("cardInfo")).thenReturn("encrypted");
        when(paymentRepository.findByUserAndCreatedAtAfter(eq(user), any(Instant.class))).thenReturn(List.of());
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        paymentService.createPayments(List.of(input), authentication);

        verify(paymentRepository).save(any(Payment.class));
        verify(paymentStatusRepository).create(anyLong());
    }

    @Test
    void createPaymentsThrowsWhenNoBankingDetails() {
        User user = new User("user1", "pass");
        PaymentInput input = new PaymentInput("john", "doe", "12345", "encrypted", BigDecimal.TEN);

        when(authentication.getName()).thenReturn("user1");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(bankingDetailsRepository.findByUser(user)).thenReturn(Optional.empty());

        BusinessRuleViolationException ex = assertThrows(BusinessRuleViolationException.class,
                () -> paymentService.createPayments(List.of(input), authentication));
        assertEquals("user user1 has no banking details, so it cannot receive payments", ex.getMessage());

        verifyNoInteractions(paymentRepository);
    }

    @Test
    void createPaymentsThrowsWhenDuplicatePaymentInLastMinute() {
        User user = new User("user1", "pass");
        PaymentInput input = new PaymentInput("john", "doe", "12345", "encrypted", BigDecimal.TEN);
        Payment existing = mock(Payment.class);
        when(existing.isSamePayment(any())).thenReturn(true);

        when(authentication.getName()).thenReturn("user1");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(bankingDetailsRepository.findByUser(user)).thenReturn(Optional.of(mock(BankingDetails.class)));
        when(cryptoService.encrypt("encrypted")).thenReturn("enc");
        when(paymentRepository.findByUserAndCreatedAtAfter(eq(user), any(Instant.class))).thenReturn(List.of(existing));

        BusinessRuleViolationException ex = assertThrows(BusinessRuleViolationException.class,
                () -> paymentService.createPayments(List.of(input), authentication));
        assertEquals("wait at least 1min to make identical payment", ex.getMessage());

        verify(paymentRepository, never()).save(any());
        verify(paymentStatusRepository, never()).create(anyLong());
    }
}