package org.example.controller;

import org.example.config.TestSecurityConfig;
import org.example.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.json.JsonCompareMode.STRICT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestSecurityConfig.class)
@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    @WithMockUser(username = "user1")
    void createPaymentSuccess() throws Exception {
        String input = Files.readString(Path.of("src/test/resources/paymentControllerTest/goodInput.json"));

        mockMvc.perform(post("/payments").with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(input))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));

        verify(paymentService).createPayments(anyList(), any(Authentication.class));
    }

    @Test
    @WithMockUser(username = "user1")
    void createPaymentInvalidCard() throws Exception {
        String input = Files.readString(Path.of("src/test/resources/paymentControllerTest/invalidCardInput.json"));
        String output = Files.readString(Path.of("src/test/resources/paymentControllerTest/invalidCardOutput.json"));

        mockMvc.perform(post("/payments").with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(input))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(output, STRICT));

        verifyNoInteractions(paymentService);
    }

    @Test
    @WithMockUser(username = "user1")
    void createPaymentExpiredCard() throws Exception {
        String input = Files.readString(Path.of("src/test/resources/paymentControllerTest/expiredCardInput.json"));
        String output = Files.readString(Path.of("src/test/resources/paymentControllerTest/expiredCardOutput.json"));

        mockMvc.perform(post("/payments").with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(input))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(output, STRICT));

        verifyNoInteractions(paymentService);
    }
}
