package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.exception.ValidationException;
import org.example.dto.input.UserInput;
import org.example.entity.User;
import org.example.repository.PaymentStatusRepository;
import org.example.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PaymentStatusRepository paymentStatusRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PaymentStatusRepository paymentStatusRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.paymentStatusRepository = paymentStatusRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void createUser(UserInput input) {
        if (userRepository.findByUsername(input.username()).isPresent())
            throw new ValidationException("username " + input.username() + " already exists");
        if (!StringUtils.hasText(input.username()) || !StringUtils.hasText(input.password()))
            throw new ValidationException("null username/password");
        if (input.username().length() > 50) throw new ValidationException("username max length is 50");
        if (input.password().length() > 50) throw new ValidationException("password max length is 50");
        userRepository.save(new User(input.username(), passwordEncoder.encode(input.password())));
        log.info("user {} created", input.username());
    }

    @Transactional
    public void deleteUser(String username) {
        paymentStatusRepository.deleteAll(userRepository.findByUsername(username).orElseThrow().getId());
        userRepository.deleteByUsername(username);
        log.info("user {} deleted", username);
    }
}