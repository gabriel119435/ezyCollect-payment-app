package org.example.controller;


import org.example.dto.input.UserInput;
import org.example.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Void> createUser(@RequestBody UserInput user) {
        userService.createUser(user);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser(Authentication authentication) {
        userService.deleteUser(authentication.getName());
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }
}