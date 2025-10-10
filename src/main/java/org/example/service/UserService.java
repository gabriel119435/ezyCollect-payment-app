package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.input.UserInput;
import org.example.dto.internal.exceptions.BusinessRuleViolationException;
import org.example.entity.User;
import org.example.repository.UserCustomRepository;
import org.example.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserCustomRepository userCustomRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserCustomRepository userCustomRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userCustomRepository = userCustomRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void createUser(UserInput input) {
        if (userRepository.findByUsername(input.username()).isPresent())
            throw new BusinessRuleViolationException("username " + input.username() + " already exists");
        userRepository.save(new User(input.username(), passwordEncoder.encode(input.password())));
        log.info("user {} created", input.username());
    }

    @Transactional
    public void deleteUser(String username) {
        long userId = userRepository.findByUsername(username).orElseThrow().getId();

        if (userCustomRepository.findPendingPaymentsAmount(userId) > 0)
            throw new BusinessRuleViolationException("user " + username + " still has pending payments");
        userCustomRepository.deleteAllDataRelatedToUser(userId);
        userRepository.deleteByUsername(username);
        log.info("user {} deleted", username);
    }
}