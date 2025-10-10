package org.example.service;

import org.example.BaseMockTest;
import org.example.dto.input.BankingDetailsInput;
import org.example.entity.BankingDetails;
import org.example.entity.User;
import org.example.repository.BankingDetailsRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class BankingDetailsServiceTest extends BaseMockTest {
    @Mock
    private BankingDetailsRepository bankingDetailsRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    Authentication authentication;
    @InjectMocks
    private BankingDetailsService bankingDetailsService;

    @Test
    void saveBankingDetailsSuccess() {
        User user = new User("user1", "pass");
        BankingDetailsInput input = new BankingDetailsInput(
                "acc123",
                "route123",
                "bankName1",
                "https://bank.webhook.com",
                "bodyTemplate1"
        );
        when(authentication.getName()).thenReturn("user1");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(bankingDetailsRepository.findByUser(user)).thenReturn(Optional.empty());

        bankingDetailsService.saveBankingDetails(input, authentication);

        verify(bankingDetailsRepository).save(any(BankingDetails.class));
    }

    @Test
    void saveExistingBankingDetailsSuccess() {
        User user = new User("user1", "pass");
        BankingDetailsInput input = new BankingDetailsInput(
                "acc123",
                "route123",
                "bankName1",
                "https://bank.webhook.com",
                "bodyTemplate1"
        );
        BankingDetails existing = mock(BankingDetails.class);
        when(authentication.getName()).thenReturn("user1");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(bankingDetailsRepository.findByUser(user)).thenReturn(Optional.of(existing));

        bankingDetailsService.saveBankingDetails(input, authentication);

        verify(existing).update(input);
        verify(bankingDetailsRepository).save(existing);
    }

    @Test
    void saveBankingDetailsUserNotFoundThrows() {
        BankingDetailsInput input = new BankingDetailsInput(
                "acc123",
                "route123",
                "bankName1",
                "https://bank.webhook.com",
                "bodyTemplate1"
        );
        when(authentication.getName()).thenReturn("user1");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.empty());

        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () ->
                bankingDetailsService.saveBankingDetails(input, authentication)
        );
        assertEquals("No value present", ex.getMessage());

        verify(bankingDetailsRepository, never()).save(any());
    }

}
