package org.example.service.processor;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.config.AsyncConfig;
import org.example.dto.internal.ExternalCallResult;
import org.example.entity.Payment;
import org.example.entity.PaymentStatus;
import org.example.entity.PaymentStatus.Status;
import org.example.repository.PaymentRepository;
import org.example.repository.PaymentStatusRepository;
import org.example.service.CryptoService;
import org.example.service.WebhookService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static org.example.entity.PaymentStatus.Status.*;

@Slf4j
@Service
public class PaymentStatusProcessor {
    public static final int DELAY_TEN_SECONDS = 10000;
    public static final int BATCH_SIZE = 10;
    private final PaymentStatusRepository paymentStatusRepository;
    private final PaymentRepository paymentRepository;
    private final CryptoService cryptoService;
    private final WebhookService webhookService;
    private final int retryLimit;

    public PaymentStatusProcessor(
            PaymentStatusRepository paymentStatusRepository,
            PaymentRepository paymentRepository,
            CryptoService cryptoService, WebhookService webhookService,

            @Value("${retry.limit}") int retryLimit
    ) {
        this.paymentStatusRepository = paymentStatusRepository;
        this.paymentRepository = paymentRepository;
        this.cryptoService = cryptoService;
        this.webhookService = webhookService;
        this.retryLimit = retryLimit;
    }

    @Scheduled(fixedDelay = DELAY_TEN_SECONDS)
    public void executePayments() {
        paymentStatusRepository.findByStatus(CREATED, BATCH_SIZE).forEach(this::execute);
        paymentStatusRepository.findByStatus(RETRY, BATCH_SIZE).forEach(this::execute);
        paymentStatusRepository.findByStatus(PROCESSED, BATCH_SIZE).forEach(this::notify);
        paymentStatusRepository.findByStatus(RETRY_NOTIFY, BATCH_SIZE).forEach(this::notify);
    }

    @Transactional
    @Async(AsyncConfig.EXECUTOR_NAME)
    private void execute(PaymentStatus ps) {
        extracted(ps, PROCESSING, Set.of(CREATED, RETRY), PROCESSED, RETRY, this::makePayment);
    }

    @Transactional
    @Async(AsyncConfig.EXECUTOR_NAME)
    private void notify(PaymentStatus ps) {
        extracted(ps, NOTIFYING, Set.of(PROCESSED, RETRY_NOTIFY), NOTIFIED, RETRY_NOTIFY, webhookService::notifyClient);
    }

    private void extracted(
            PaymentStatus ps,
            Status intermediaryStatus,
            Set<Status> expectedStatuses,
            Status successStatus,
            Status retryStatus,
            Function<PaymentStatus, ExternalCallResult> operation
    ) {
        Status previousStatus = ps.getStatus();
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
            ExternalCallResult result = operation.apply(ps);

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

    private ExternalCallResult makePayment(PaymentStatus ps) {
        try {
            Optional<Payment> optionalPayment = paymentRepository.findById(ps.getPaymentId());

            if (optionalPayment.isEmpty()) return new ExternalCallResult(false, "missing payment");

            double random = Math.random();
            if (random < 0.1) throw new RuntimeException("runtime cause: " + random);
            if (random < 0.5) {
                log.info("payment_status {} failed", ps.getId());
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
