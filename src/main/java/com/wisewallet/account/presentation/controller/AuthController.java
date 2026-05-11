package com.wisewallet.account.presentation.controller;

import com.wisewallet.account.application.command.AuthCommandService;
import com.wisewallet.account.presentation.dto.request.LoginRequest;
import com.wisewallet.account.presentation.dto.request.RefreshTokenRequest;
import com.wisewallet.account.presentation.dto.request.RegisterRequest;
import com.wisewallet.account.presentation.dto.response.LoginResponse;
import com.wisewallet.account.presentation.dto.response.RegisterResponse;
import com.wisewallet.account.presentation.dto.response.VerifyEmailResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthCommandService authCommandService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        var result = authCommandService.register(
                request.email(), request.password(), request.firstName(), request.lastName());
        return new RegisterResponse(result.userId(), result.email(),
                "Registered. Please verify your email.");
    }

    @GetMapping("/verify")
    public VerifyEmailResponse verifyEmail(@RequestParam String token) {
        authCommandService.verifyEmail(token);
        return new VerifyEmailResponse("Email verified successfully.");
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        var result = authCommandService.login(request.email(), request.password());
        return LoginResponse.of(result.accessToken(), result.refreshToken());
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        var result = authCommandService.refresh(request.refreshToken());
        return LoginResponse.of(result.accessToken(), result.refreshToken());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader("X-User-Id") String userId) {
        authCommandService.logout(UUID.fromString(userId));
    }
}
