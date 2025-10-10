package org.example.service;

import org.springframework.stereotype.Service;

@Service
public class RandomProvider {
    // for tests
    public double random() {
        return Math.random();
    }
}
