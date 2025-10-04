package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.dto.input.PaymentInput;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Entity
@Table(name = "payments")
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String firstName;
    private String lastName;
    private String zipCode;
    private String cardInfo;
    private BigDecimal paymentValue;
    @Column(updatable = false, insertable = false)
    private Instant createdAt;

    public Payment(User user, PaymentInput input) {
        this.user = user;
        this.firstName = input.firstName();
        this.lastName = input.lastName();
        this.zipCode = input.zipCode();
        this.cardInfo = input.cardInfo();
        this.paymentValue = input.paymentValue();
    }
}