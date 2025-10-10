package org.example.service;


import org.example.BaseMockTest;
import org.example.dto.input.UserInput;
import org.example.dto.internal.exceptions.BusinessRuleViolationException;
import org.example.entity.User;
import org.example.repository.UserCustomRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserServiceTest extends BaseMockTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserCustomRepository userCustomRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    @Test
    void createUserSuccess() {
        UserInput input = new UserInput("user1", "pass1");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass1")).thenReturn("encoded");

        userService.createUser(input);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUserUsernameExistsThrows() {
        UserInput input = new UserInput("user1", "pass1");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(new User()));

        assertThrows(BusinessRuleViolationException.class, () -> userService.createUser(input));

        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUserSuccess() {
        User user = new User("user1", "encoded");
        user.setId(1L);
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(userCustomRepository.findPendingPaymentsAmount(1L)).thenReturn(0);

        userService.deleteUser("user1");

        verify(userCustomRepository).deleteAllDataRelatedToUser(1L);
        verify(userRepository).deleteByUsername("user1");
    }

    @Test
    void deleteUserPendingPaymentsThrows() {
        User user = new User("user1", "encoded");
        user.setId(1L);
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(userCustomRepository.findPendingPaymentsAmount(1L)).thenReturn(100);

        assertThrows(BusinessRuleViolationException.class, () -> userService.deleteUser("user1"));

        verify(userCustomRepository, never()).deleteAllDataRelatedToUser(anyLong());
        verify(userRepository, never()).deleteByUsername(any());
    }
}