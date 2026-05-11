package com.wisewallet.account.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wisewallet.account.application.command.AuthCommandService;
import com.wisewallet.account.domain.exception.InvalidCredentialsException;
import com.wisewallet.account.domain.exception.UserAlreadyExistsException;
import com.wisewallet.account.presentation.dto.request.LoginRequest;
import com.wisewallet.account.presentation.dto.request.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AuthCommandService authCommandService;

    @Test
    void register_validRequest_returns201() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authCommandService.register(any(), any(), any(), any()))
                .thenReturn(new AuthCommandService.RegisterResult(userId, "john@example.com"));

        var request = new RegisterRequest("john@example.com", "password123", "John", "Doe");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void register_invalidEmail_returns400WithErrors() throws Exception {
        var request = new RegisterRequest("not-an-email", "password123", "John", "Doe");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        when(authCommandService.register(any(), any(), any(), any()))
                .thenThrow(new UserAlreadyExistsException("john@example.com"));

        var request = new RegisterRequest("john@example.com", "password123", "John", "Doe");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        when(authCommandService.login(any(), any()))
                .thenThrow(new InvalidCredentialsException());

        var request = new LoginRequest("user@example.com", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_validCredentials_returns200WithTokenShape() throws Exception {
        when(authCommandService.login(any(), any()))
                .thenReturn(new AuthCommandService.LoginResult("access-jwt", "refresh-raw"));

        var request = new LoginRequest("user@example.com", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-jwt"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(jsonPath("$.refreshToken").value("refresh-raw"));
    }
}
