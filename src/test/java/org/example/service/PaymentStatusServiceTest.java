package org.example.service;


import org.example.BaseMockTest;
import org.example.dto.internal.ExternalCallResult;
import org.example.entity.PaymentStatus;
import org.example.repository.PaymentStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.Instant;
import java.util.Set;

import static org.example.entity.PaymentStatus.Status.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PaymentStatusServiceTest extends BaseMockTest {

    @Mock
    private PaymentStatusRepository paymentStatusRepository;
    @Mock
    private ExternalCallService externalCallService;
    private PaymentStatusService service;

    @BeforeEach
    void setUp() {
        service = new PaymentStatusService(paymentStatusRepository, externalCallService, 3);
    }

    private static PaymentStatus getPaymentStatus(PaymentStatus.Status status, int retries) {
        return new PaymentStatus(
                1L, 2L, status, retries,
                "", Instant.now(), Instant.now()
        );
    }

    @Test
    void updateStatusWithSuccessPayment() {
        PaymentStatus ps = getPaymentStatus(CREATED, 0);

        when(paymentStatusRepository.tryToUpdate(any(), anyInt(), anyString(), any(), anyLong(), anySet(), any()))
                .thenReturn(1);
        when(externalCallService.makePayment(ps)).thenReturn(new ExternalCallResult(true, "paid"));

        service.updateStatusWithExternalCall(ps, PROCESSING,
                Set.of(CREATED),
                PROCESSED, RETRY, true);

        assert (ps.getStatus() == PROCESSED);
        verify(paymentStatusRepository, times(2)).tryToUpdate(any(), anyInt(), anyString(), any(), anyLong(), anySet(), any());
    }


    @Test
    void updateStatusWithFailPayment() {
        PaymentStatus ps = getPaymentStatus(CREATED, 0);

        when(paymentStatusRepository.tryToUpdate(
                any(), anyInt(), anyString(), any(), anyLong(), anySet(), any())
        ).thenReturn(1);
        when(externalCallService.makePayment(ps)).thenReturn(new ExternalCallResult(false, "fail"));

        service.updateStatusWithExternalCall(ps, PROCESSING,
                Set.of(CREATED),
                RETRY, RETRY, true);

        assert (ps.getStatus() == RETRY);
        assert (ps.getRetries() == 1);
    }

    @Test
    void updateStatusWithRetryLimitReached() {
        PaymentStatus ps = getPaymentStatus(RETRY, 2);

        when(paymentStatusRepository.tryToUpdate(
                any(), anyInt(), anyString(), any(), anyLong(), anySet(), any())
        ).thenReturn(1);
        when(externalCallService.makePayment(ps)).thenReturn(new ExternalCallResult(false, "fail"));

        service.updateStatusWithExternalCall(ps, PROCESSING,
                Set.of(RETRY),
                PROCESSED, RETRY, true);

        assert (ps.getStatus() == PaymentStatus.Status.ERROR);
    }
}
