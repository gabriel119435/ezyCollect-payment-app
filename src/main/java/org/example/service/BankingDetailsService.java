package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.exception.ValidationException;
import org.example.dto.input.BankingDetailsInput;
import org.example.entity.BankingDetails;
import org.example.entity.User;
import org.example.repository.BankingDetailsRepository;
import org.example.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ValidationException("user " + currentUsername + " logged in but not found"));

        if (!StringUtils.hasText(input.bankName()) || !StringUtils.hasText(input.accountNumber()) ||
                !StringUtils.hasText(input.routingNumber()) || !StringUtils.hasText(input.webhookUrl()) ||
                !StringUtils.hasText(input.bodyTemplate())
        ) throw new ValidationException("null bankName/accountNumber/routingNumber/webhookUrl/bodyTemplate");

        if (input.bankName().length() > 100) throw new ValidationException("bankName max length is 100");
        if (input.accountNumber().length() > 50) throw new ValidationException("accountNumber max length is 50");
        if (input.routingNumber().length() > 50) throw new ValidationException("routingNumber max length is 50");
        if (input.webhookUrl().length() > 200) throw new ValidationException("webhookUrl max length is 200");

        if (input.bodyTemplate().length() > 500) throw new ValidationException("bodyTemplate max length is 500");
        TemplateEngine.validateTemplate(input.bodyTemplate());

        Optional<BankingDetails> optional = bankingDetailsRepository.findByUser(currentUser);

        optional.ifPresentOrElse(previous -> {
            previous.update(input);
            bankingDetailsRepository.save(previous);
        }, () -> bankingDetailsRepository.save(new BankingDetails(currentUser, input)));
        log.info("banking details updated for user {}", currentUser.getUsername());
    }

}
