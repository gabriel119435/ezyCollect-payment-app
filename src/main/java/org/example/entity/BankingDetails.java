package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.dto.input.BankingDetailsInput;

import java.time.Instant;

@Data
@Entity
@Table
@NoArgsConstructor
public class BankingDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String accountNumber;
    private String routingNumber;
    private String bankName;
    private String webhookUrl;
    private String bodyTemplate;
    private Instant createdAt;

    public void update(BankingDetailsInput input) {
        this.accountNumber = input.accountNumber();
        this.routingNumber = input.routingNumber();
        this.bankName = input.bankName();
        this.webhookUrl = input.webhookUrl();
        this.bodyTemplate = input.bodyTemplate();
        this.createdAt = Instant.now();
    }

    public BankingDetails(User user, BankingDetailsInput input) {
        this.user = user;
        this.update(input);
    }
}