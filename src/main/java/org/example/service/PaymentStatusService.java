package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.internal.ExternalCallResult;
import org.example.dto.internal.exceptions.BadConfigurationException;
import org.example.entity.PaymentStatus;
import org.example.repository.PaymentStatusRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Set;

import static org.example.entity.PaymentStatus.Status.ERROR;

@Slf4j
@Service
public class PaymentStatusService {

    private final PaymentStatusRepository paymentStatusRepository;

    private final ExternalCallService externalCallService;
    private final int retryLimit;
    private final int retryLimitNotify;

    public PaymentStatusService(
            PaymentStatusRepository paymentStatusRepository,
            ExternalCallService externalCallService,
            @Value("${retry.limit}") int retryLimit,
            @Value("${retry.limit.notify}") int retryLimitNotify
    ) {
        if (!(retryLimit < retryLimitNotify))
            throw new BadConfigurationException("desired configuration: retryLimit < retryLimitNotify");

        this.paymentStatusRepository = paymentStatusRepository;
        this.externalCallService = externalCallService;
        this.retryLimit = retryLimit;
        this.retryLimitNotify = retryLimitNotify;
    }

    public void updateStatusWithExternalCall(
            PaymentStatus ps,
            PaymentStatus.Status intermediaryStatus,
            Set<PaymentStatus.Status> expectedStatuses,
            PaymentStatus.Status successStatus,
            PaymentStatus.Status retryStatus,
            boolean shouldPayOrNotify
    ) {

        if (ps.getRetries() >= retryLimitNotify) {
            log.info("payment_status {} reached max retries {}. skipping processing", ps.getId(), ps.getRetries());
            return;
        }

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
                    ? externalCallService.makePayment(ps)
                    : externalCallService.notifyClient(ps);

            if (result.success()) {
                ps.setStatus(successStatus);
            } else {
                ps.setRetries(ps.getRetries() + 1);
                boolean lastRetry = ps.getRetries() >= retryLimit;
                ps.setStatus(lastRetry ? ERROR : retryStatus);
            }

            ps.setHistory(
                    StringUtils.hasText(ps.getHistory())
                            ? ps.getHistory() + " | " + result.message()
                            : result.message()
            );


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
}
