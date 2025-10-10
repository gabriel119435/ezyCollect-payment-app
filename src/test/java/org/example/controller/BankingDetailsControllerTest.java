package org.example.controller;


import org.example.config.TestSecurityConfig;
import org.example.dto.input.BankingDetailsInput;
import org.example.service.BankingDetailsService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.json.JsonCompareMode.STRICT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestSecurityConfig.class)
@WebMvcTest(BankingDetailsController.class)
class BankingDetailsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BankingDetailsService bankingDetailsService;

    @Test
    @WithMockUser(username = "user1")
    void saveBankingDetailsReturnsCreated() throws Exception {
        String input = Files.readString(Path.of("src/test/resources/bankingDetailsControllerTest/goodInput.json"));

        mockMvc.perform(put("/banking-details").with(csrf())
                        .contentType(APPLICATION_JSON).content(input))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));

        verify(bankingDetailsService).saveBankingDetails(any(BankingDetailsInput.class), any(Authentication.class));
    }

    @Test
    @WithMockUser(username = "user1")
    void saveBankingDetailsInvalidUrlReturnsBadRequest() throws Exception {
        String input = Files.readString(Path.of("src/test/resources/bankingDetailsControllerTest/invalidUrlInput.json"));
        String output = Files.readString(Path.of("src/test/resources/bankingDetailsControllerTest/invalidUrlOutput.json"));

        mockMvc.perform(put("/banking-details").with(csrf())
                        .contentType(APPLICATION_JSON).content(input))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(output, STRICT));

        verifyNoInteractions(bankingDetailsService);
    }

    @Test
    @WithMockUser(username = "user1")
    void saveBankingDetailsInvalidTemplateReturnsBadRequest() throws Exception {
        String input = Files.readString(Path.of("src/test/resources/bankingDetailsControllerTest/invalidTemplateInput.json"));
        String output = Files.readString(Path.of("src/test/resources/bankingDetailsControllerTest/invalidTemplateOutput.json"));

        mockMvc.perform(put("/banking-details").with(csrf())
                        .contentType(APPLICATION_JSON).content(input))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(output, STRICT));

        verifyNoInteractions(bankingDetailsService);
    }
}
