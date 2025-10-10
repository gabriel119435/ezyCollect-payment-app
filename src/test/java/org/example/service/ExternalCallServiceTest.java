package org.example.service;


import org.example.BaseMockTest;
import org.example.dto.internal.ExternalCallResult;
import org.example.entity.BankingDetails;
import org.example.entity.Payment;
import org.example.entity.PaymentStatus;
import org.example.entity.User;
import org.example.repository.BankingDetailsRepository;
import org.example.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Optional;

import static org.example.entity.PaymentStatus.Status.CREATED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExternalCallServiceTest extends BaseMockTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private BankingDetailsRepository bankingDetailsRepository;
    @Mock
    private CryptoService cryptoService;
    @Mock
    private HttpClient httpClient;
    @Mock
    private RandomProvider randomProvider;
    @InjectMocks
    private ExternalCallService service;

    @Test
    void makePaymentMissingPaymentFail() {
        PaymentStatus ps = new PaymentStatus(1, 1, CREATED, 0, "", Instant.now(), Instant.now());
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        ExternalCallResult result = service.makePayment(ps);
        assertFalse(result.success());
        assertEquals("missing payment", result.message());
    }

    @Test
    void makePaymentSuccess() {
        PaymentStatus ps = new PaymentStatus(1, 1, CREATED, 0, "", Instant.now(), Instant.now());
        Payment payment = new Payment();
        payment.setCardInfo("encrypted");
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(cryptoService.decrypt("encrypted")).thenReturn("1234");
        when(randomProvider.random()).thenReturn(0.6);

        ExternalCallResult result = service.makePayment(ps);
        assertTrue(result.success());
        assertEquals("paid", result.message());
    }

    @Test
    void notifyClientSuccess() throws Exception {
        User user = new User();
        user.setUsername("user1");

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setFirstName("john");
        payment.setLastName("doe");
        payment.setPaymentValue(BigDecimal.TEN);
        payment.setCardInfo("encrypted");

        BankingDetails bd = new BankingDetails();
        bd.setBankName("bank");
        bd.setRoutingNumber("111");
        bd.setAccountNumber("222");
        bd.setWebhookUrl("http://example.com");
        bd.setBodyTemplate("template");

        PaymentStatus ps = new PaymentStatus(1, 1, CREATED, 0, "history", Instant.now(), Instant.now());

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(bankingDetailsRepository.findByUser(user)).thenReturn(Optional.of(bd));

        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("ok");
        when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);

        ExternalCallResult result = service.notifyClient(ps);
        assertTrue(result.success());
        assertTrue(result.message().contains("200 ok"));
    }

    @Test
    void notifyClientMissingBankingDetails() {
        User user = new User();
        Payment payment = new Payment();
        payment.setUser(user);
        PaymentStatus ps = new PaymentStatus(1, 1, CREATED, 0, "", Instant.now(), Instant.now());

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(bankingDetailsRepository.findByUser(user)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.notifyClient(ps));
        assertEquals("user " + user + " does not have banking details", ex.getMessage());
    }
}
