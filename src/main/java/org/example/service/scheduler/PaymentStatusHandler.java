package org.example.service.scheduler;


import org.example.config.AsyncConfig;
import org.example.entity.PaymentStatus;
import org.example.service.PaymentStatusService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.example.entity.PaymentStatus.Status.*;

@Service
public class PaymentStatusHandler {

    private final PaymentStatusService paymentStatusService;

    public PaymentStatusHandler(PaymentStatusService paymentStatusService) {
        this.paymentStatusService = paymentStatusService;
    }

    @Async(AsyncConfig.EXECUTOR_NAME)
    @Transactional
    public void processPayment(PaymentStatus ps) {
        paymentStatusService.updateStatusWithExternalCall(ps, PROCESSING, Set.of(CREATED, RETRY), PROCESSED, RETRY, true);
    }

    @Async(AsyncConfig.EXECUTOR_NAME)
    @Transactional
    public void sendNotification(PaymentStatus ps) {
        paymentStatusService.updateStatusWithExternalCall(ps, NOTIFYING, Set.of(PROCESSED, RETRY_NOTIFY), NOTIFIED, RETRY_NOTIFY, false);
    }
}