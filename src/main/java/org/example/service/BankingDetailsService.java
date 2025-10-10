package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.input.BankingDetailsInput;
import org.example.entity.BankingDetails;
import org.example.entity.User;
import org.example.repository.BankingDetailsRepository;
import org.example.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class BankingDetailsService {
    private final BankingDetailsRepository bankingDetailsRepository;
    private final UserRepository userRepository;

    public BankingDetailsService(BankingDetailsRepository bankingDetailsRepository, UserRepository userRepository) {
        this.bankingDetailsRepository = bankingDetailsRepository;
        this.userRepository = userRepository;
    }

    public void saveBankingDetails(BankingDetailsInput input, Authentication authentication) {
        User currentUser = userRepository.findByUsername(authentication.getName()).orElseThrow();
        Optional<BankingDetails> optional = bankingDetailsRepository.findByUser(currentUser);

        optional.ifPresentOrElse(previous -> {
            previous.update(input);
            bankingDetailsRepository.save(previous);
        }, () -> bankingDetailsRepository.save(new BankingDetails(currentUser, input)));
        log.info("banking details updated for user {}", currentUser.getUsername());
    }
}
