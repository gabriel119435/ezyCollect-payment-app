package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.exception.ValidationException;
import org.example.dto.input.PaymentInput;
import org.example.entity.Payment;
import org.example.entity.User;
import org.example.repository.BankingDetailsRepository;
import org.example.repository.PaymentRepository;
import org.example.repository.PaymentStatusRepository;
import org.example.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class PaymentService {

    private static final Pattern CARD_PATTERN = Pattern.compile("^(\\d{4} \\d{4} \\d{4} \\d{4}) (\\d{2}/\\d{2}) (\\d{3})$");

    private final UserRepository userRepository;
    private final BankingDetailsRepository bankingDetailsRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentStatusRepository paymentStatusRepository;
    private final CryptoService cryptoService;

    public PaymentService(
            UserRepository userRepository,
            BankingDetailsRepository bankingDetailsRepository,
            PaymentRepository paymentRepository,
            PaymentStatusRepository paymentStatusRepository,
            CryptoService cryptoService
    ) {
        this.userRepository = userRepository;
        this.bankingDetailsRepository = bankingDetailsRepository;
        this.paymentRepository = paymentRepository;
        this.paymentStatusRepository = paymentStatusRepository;
        this.cryptoService = cryptoService;
    }

    public void createPayment(PaymentInput input, Authentication authentication) {
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ValidationException("user " + currentUsername + " logged in but not found"));

        if (bankingDetailsRepository.findByUser(currentUser).isEmpty())
            throw new ValidationException("user " + currentUser.getUsername() + " has no banking details, so it cannot receive payments");

        if (!StringUtils.hasText(input.firstName()) || !StringUtils.hasText(input.lastName()) ||
                !StringUtils.hasText(input.zipCode()) || !StringUtils.hasText(input.cardInfo())
        ) throw new ValidationException("null firstName/lastName/zipCode/cardInfo");

        if (input.paymentValue() == null || BigDecimal.ZERO.compareTo(input.paymentValue()) >= 0)
            throw new ValidationException("paymentValue should be positive");

        if (input.firstName().length() > 50) throw new ValidationException("firstName max length is 50");
        if (input.lastName().length() > 50) throw new ValidationException("firstName max length is 50");
        if (input.zipCode().length() > 20) throw new ValidationException("zipcode max length is 20");

        if (!isCardValid(input.cardInfo()))
            throw new ValidationException("invalid card info, format 1234 1234 1234 1234 12/99 123");

        PaymentInput cryptoInput = new PaymentInput(
                input.firstName(),
                input.lastName(),
                input.zipCode(),
                cryptoService.encrypt(input.cardInfo()),
                input.paymentValue()
        );

        List<Payment> paymentsInLastMinute = paymentRepository.findByCreatedAtAfter(Instant.now().minusSeconds(60));

        boolean hasSamePaymentInLastMinute = paymentsInLastMinute
                .stream().anyMatch(p -> Objects.equals(p.getFirstName(), cryptoInput.firstName())
                        && Objects.equals(p.getLastName(), cryptoInput.lastName())
                        && Objects.equals(p.getZipCode(), cryptoInput.zipCode())
                        && Objects.equals(p.getCardInfo(), cryptoInput.cardInfo())
                        && p.getPaymentValue().compareTo(cryptoInput.paymentValue()) == 0
                );

        if (hasSamePaymentInLastMinute) throw new ValidationException("wait at least 1min to make identical payment");

        Payment payment = paymentRepository.save(new Payment(currentUser, cryptoInput));
        paymentStatusRepository.create(payment.getId());
        log.info("payment created for user {}", currentUser.getUsername());
    }

    private boolean isCardValid(String cardInfo) {
        Matcher m = CARD_PATTERN.matcher(cardInfo);
        if (m.matches()) {
            String date = m.group(2);
            int expiryMonth = Integer.parseInt(date.split("/")[0]);
            int expiryYear = Integer.parseInt(date.split("/")[1]);
            int currentYear = LocalDateTime.now().getYear();
            int currentMonth = LocalDateTime.now().getMonthValue();
            return currentYear < expiryYear || currentMonth < expiryMonth;
        }
        return false;
    }
}
