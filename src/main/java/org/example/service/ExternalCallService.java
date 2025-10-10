package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.internal.ExternalCallResult;
import org.example.entity.BankingDetails;
import org.example.entity.Payment;
import org.example.entity.PaymentStatus;
import org.example.entity.User;
import org.example.repository.BankingDetailsRepository;
import org.example.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.example.service.TemplateRenderer.TemplateKey.*;

@Slf4j
@Service
public class ExternalCallService {

    private final PaymentRepository paymentRepository;
    private final BankingDetailsRepository bankingDetailsRepository;
    private final CryptoService cryptoService;
    private final HttpClient httpClient;
    private final RandomProvider randomProvider;

    public ExternalCallService(
            PaymentRepository paymentRepository,
            BankingDetailsRepository bankingDetailsRepository,
            CryptoService cryptoService,
            HttpClient httpClient,
            RandomProvider randomProvider
    ) {
        this.paymentRepository = paymentRepository;
        this.bankingDetailsRepository = bankingDetailsRepository;
        this.cryptoService = cryptoService;
        this.httpClient = httpClient;
        this.randomProvider = randomProvider;
    }

    public ExternalCallResult makePayment(PaymentStatus ps) {
        try {
            Optional<Payment> optionalPayment = paymentRepository.findById(ps.getPaymentId());

            if (optionalPayment.isEmpty()) return new ExternalCallResult(false, "missing payment");

            double random = randomProvider.random();
            if (random < 0.1) throw new RuntimeException("runtime random cause: " + random);
            if (random < 0.5) {
                log.info("payment_status {} randomly failed", ps.getId());
                return new ExternalCallResult(false, "failed with error " + random);
            }

            Payment payment = optionalPayment.get();
            String rawCardInfo = cryptoService.decrypt(payment.getCardInfo());

            log.info("payment_status {} was successful with card info {}", ps.getId(), rawCardInfo);
            return new ExternalCallResult(true, "paid");
        } catch (Exception e) {
            String message = "error during " + ps;
            log.error(message, e);
            return new ExternalCallResult(false, e.getMessage());
        }
    }

    public ExternalCallResult notifyClient(PaymentStatus paymentStatus) {
        Payment payment = paymentRepository.findById(paymentStatus.getPaymentId()).orElseThrow(
                () -> new IllegalArgumentException("paymentStatus " + paymentStatus + " does not have a user")
        );

        User user = payment.getUser();

        BankingDetails bankingDetails = bankingDetailsRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("user " + user + " does not have banking details"));

        Map<String, String> values = buildMap(paymentStatus, user, bankingDetails, payment);

        String url = bankingDetails.getWebhookUrl();
        String body = TemplateRenderer.render(bankingDetails.getBodyTemplate(), values);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "text/plain")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            boolean success = response.statusCode() >= 200 && response.statusCode() < 300;
            return new ExternalCallResult(success, buildResultResponse(response));
        } catch (Exception e) {
            return new ExternalCallResult(false, e.getMessage());
        }
    }

    private static String buildResultResponse(HttpResponse<String> response) {
        String baseMessage = (response.statusCode() + " " + response.body()).replaceAll("\\s+", " ");
        return baseMessage.length() <= 400
                ? baseMessage
                : baseMessage.substring(0, 200) + "..." + baseMessage.substring(baseMessage.length() - 200);
    }

    private static Map<String, String> buildMap(PaymentStatus paymentStatus, User user, BankingDetails bankingDetails, Payment payment) {
        Map<String, String> values = new HashMap<>();
        values.put(USERNAME.getKey(), user.getUsername());
        values.put(BANK_NAME.getKey(), bankingDetails.getBankName());
        values.put(ROUTING_NUMBER.getKey(), bankingDetails.getRoutingNumber());
        values.put(ACCOUNT_NUMBER.getKey(), bankingDetails.getAccountNumber());
        values.put(PAYMENT_FIRST_NAME.getKey(), payment.getFirstName());
        values.put(PAYMENT_LAST_NAME.getKey(), payment.getLastName());
        values.put(PAYMENT_VALUE.getKey(), payment.getPaymentValue().toPlainString());
        values.put(PAYMENT_STATUS_STATUS.getKey(), paymentStatus.getStatus().name());
        values.put(PAYMENT_STATUS_HISTORY.getKey(), paymentStatus.getHistory());
        values.put(PAYMENT_STATUS_UPDATED_AT.getKey(), paymentStatus.getUpdatedAt().toString());
        return values;
    }
}
