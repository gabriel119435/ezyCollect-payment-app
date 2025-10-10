package org.example.service.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.example.repository.PaymentStatusRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import static org.example.entity.PaymentStatus.Status.*;

@Slf4j
@Service
public class PaymentStatusScheduler {
    public static final int DELAY_TEN_SECONDS = 10000;
    public static final int BATCH_SIZE = 10;
    private final PaymentStatusRepository paymentStatusRepository;
    private final PaymentStatusHandler paymentStatusHandler;

    public PaymentStatusScheduler(
            PaymentStatusRepository paymentStatusRepository, PaymentStatusHandler paymentStatusHandler
    ) {
        this.paymentStatusRepository = paymentStatusRepository;
        this.paymentStatusHandler = paymentStatusHandler;
    }

    @Scheduled(fixedDelay = DELAY_TEN_SECONDS)
    public void processPendingPayments() {
        paymentStatusRepository.findByStatus(CREATED, BATCH_SIZE).forEach(paymentStatusHandler::processPayment);
        paymentStatusRepository.findByStatus(RETRY, BATCH_SIZE).forEach(paymentStatusHandler::processPayment);
        paymentStatusRepository.findByStatus(PROCESSED, BATCH_SIZE).forEach(paymentStatusHandler::sendNotification);
        paymentStatusRepository.findByStatus(RETRY_NOTIFY, BATCH_SIZE).forEach(paymentStatusHandler::sendNotification);
    }
}
