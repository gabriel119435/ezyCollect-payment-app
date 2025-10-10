package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.internal.ExternalCallResult;
import org.example.entity.Payment;
import org.example.entity.PaymentStatus;
import org.example.repository.PaymentRepository;
import org.example.repository.PaymentStatusRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.example.entity.PaymentStatus.Status.ERROR;

@Slf4j
@Service
public class PaymentStatusService {

    private final PaymentStatusRepository paymentStatusRepository;
    private final PaymentRepository paymentRepository;
    private final CryptoService cryptoService;
    private final WebhookService webhookService;
    private final int retryLimit;

    public PaymentStatusService(
            PaymentStatusRepository paymentStatusRepository,
            PaymentRepository paymentRepository,
            CryptoService cryptoService,
            WebhookService webhookService,
            @Value("${retry.limit}") int retryLimit
    ) {
        this.paymentStatusRepository = paymentStatusRepository;
        this.paymentRepository = paymentRepository;
        this.cryptoService = cryptoService;
        this.webhookService = webhookService;
        this.retryLimit = retryLimit;
    }

    public void updateStatusWithExternalCall(
            PaymentStatus ps,
            PaymentStatus.Status intermediaryStatus,
            Set<PaymentStatus.Status> expectedStatuses,
            PaymentStatus.Status successStatus,
            PaymentStatus.Status retryStatus,
            boolean shouldPayOrNotify
    ) {
        PaymentStatus.Status previousStatus = ps.getStatus();
        Instant instantToProcessing = Instant.now();
        boolean acquiredLock = paymentStatusRepository.tryToUpdate(
                intermediaryStatus,
                ps.getRetries(),
                ps.getHistory(),
                instantToProcessing,
                ps.getId(),
                expectedStatuses,
                ps.getUpdatedAt()
        ) == 1;

        if (acquiredLock) {
            ExternalCallResult result = shouldPayOrNotify
                    ? this.makePayment(ps)
                    : webhookService.notifyClient(ps);

            ps.setHistory(
                    StringUtils.hasText(ps.getHistory())
                            ? ps.getHistory() + " | " + result.message()
                            : result.message()
            );

            if (result.success()) {
                ps.setStatus(successStatus);
            } else {
                ps.setRetries(ps.getRetries() + 1);
                boolean lastRetry = ps.getRetries() == retryLimit;
                ps.setStatus(lastRetry ? ERROR : retryStatus);
            }
            int updatedRows = paymentStatusRepository.tryToUpdate(
                    ps.getStatus(),
                    ps.getRetries(),
                    ps.getHistory(),
                    Instant.now(),
                    ps.getId(),
                    Set.of(intermediaryStatus),
                    instantToProcessing
            );
            if (updatedRows == 1)
                log.info("payment_status {} was updated from {} to {} with result {}", ps.getId(), previousStatus, ps.getStatus(), result.message());
        }
    }

    public ExternalCallResult makePayment(PaymentStatus ps) {
        try {
            Optional<Payment> optionalPayment = paymentRepository.findById(ps.getPaymentId());

            if (optionalPayment.isEmpty()) return new ExternalCallResult(false, "missing payment");

            double random = Math.random();
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
}
