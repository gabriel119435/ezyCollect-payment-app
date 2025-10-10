package org.example.repository;

import org.example.entity.Payment;
import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;


public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUserAndCreatedAtAfter(User user, Instant createdAt);
}
