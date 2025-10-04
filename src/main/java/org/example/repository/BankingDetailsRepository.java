package org.example.repository;

import org.example.entity.BankingDetails;
import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankingDetailsRepository extends JpaRepository<BankingDetails, Long> {
    Optional<BankingDetails> findByUser(User user);
}
