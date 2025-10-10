package org.example.controller;

import org.example.config.TestSecurityConfig;
import org.example.dto.input.UserInput;
import org.example.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestSecurityConfig.class)
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void createUserReturnsCreated() throws Exception {
        String json = """
                {"username":"user1","password":"valid_password"}
                """;

        mockMvc.perform(post("/users").with(csrf())
                        .contentType(APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));

        verify(userService).createUser(any(UserInput.class));
    }

    @Test
    @WithMockUser(username = "user1")
    void deleteUserReturnsOk() throws Exception {
        mockMvc.perform(delete("/users").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(userService).deleteUser("user1");
    }

    @Test
    void createUserInvalidReturnsBadRequest() throws Exception {
        String response = Files.readString(Path.of("src/test/resources/userControllerTest/invalidResponse.json"));
        String json = """
                {"username":"u1","password":"short"}
                """;

        mockMvc.perform(post("/users").with(csrf())
                        .contentType(APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(response, STRICT));

        verifyNoInteractions(userService);
    }
}