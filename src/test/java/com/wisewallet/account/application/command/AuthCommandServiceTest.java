package com.wisewallet.account.application.command;

import com.wisewallet.account.application.port.out.EmailPort;
import com.wisewallet.account.application.port.out.TokenIssuerPort;
import com.wisewallet.account.application.port.out.TokenStorePort;
import com.wisewallet.account.domain.exception.InvalidCredentialsException;
import com.wisewallet.account.domain.exception.UserAlreadyExistsException;
import com.wisewallet.account.domain.model.*;
import com.wisewallet.account.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthCommandServiceTest {

    @Mock UserRepositoryPort userRepository;
    @Mock AccountRepositoryPort accountRepository;
    @Mock AccountBalanceRepositoryPort balanceRepository;
    @Mock RefreshTokenRepositoryPort refreshTokenRepository;
    @Mock EmailVerificationTokenRepositoryPort verificationTokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock TokenIssuerPort tokenIssuer;
    @Mock TokenStorePort tokenStore;
    @Mock EmailPort emailPort;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks
    AuthCommandService authCommandService;

    @BeforeEach
    void setup() {
        when(tokenIssuer.getAccessTokenTtlSeconds()).thenReturn(900L);
        when(tokenIssuer.getRefreshTokenTtlDays()).thenReturn(7L);
    }

    @Test
    void register_newUser_succeeds() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(balanceRepository.save(any(AccountBalance.class))).thenAnswer(inv -> inv.getArgument(0));
        when(verificationTokenRepository.save(any(EmailVerificationToken.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = authCommandService.register("test@example.com", "password123", "John", "Doe");

        assertThat(result.email()).isEqualTo("test@example.com");
        verify(emailPort).sendVerification(eq("test@example.com"), anyString(), eq("John"));
        verify(eventPublisher).publishEvent((Object) any());
    }

    @Test
    void register_duplicateEmail_throwsUserAlreadyExistsException() {
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authCommandService.register("existing@example.com", "pass", "A", "B"))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    void login_validCredentials_returnsTokens() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId).email("u@e.com").passwordHash("hashed")
                .status(UserStatus.ACTIVE).roles(Set.of("ROLE_USER"))
                .balanceLowThreshold(BigDecimal.valueOf(100)).build();

        when(userRepository.findByEmail("u@e.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashed")).thenReturn(true);
        when(accountRepository.findByUserIdAndStatusNot(userId, AccountStatus.CLOSED)).thenReturn(List.of());
        when(tokenIssuer.issueAccessToken(any(), any(), any(), any())).thenReturn("jwt-token");
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode(anyString())).thenReturn("refresh-hash");

        var result = authCommandService.login("u@e.com", "password");
        assertThat(result.accessToken()).isEqualTo("jwt-token");
        assertThat(result.refreshToken()).contains(userId.toString());
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentialsException() {
        User user = User.builder()
                .id(UUID.randomUUID()).email("u@e.com").passwordHash("hashed")
                .status(UserStatus.ACTIVE).roles(Set.of("ROLE_USER"))
                .balanceLowThreshold(BigDecimal.valueOf(100)).build();

        when(userRepository.findByEmail("u@e.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authCommandService.login("u@e.com", "wrong"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void logout_blocksJtiAndRevokesToken() {
        UUID userId = UUID.randomUUID();
        UUID jti = UUID.randomUUID();
        RefreshToken rt = RefreshToken.builder()
                .id(UUID.randomUUID()).userId(userId).tokenHash("hash")
                .accessJti(jti).revoked(false)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS)).build();

        when(refreshTokenRepository.findActiveByUserId(userId)).thenReturn(Optional.of(rt));
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        authCommandService.logout(userId);

        verify(tokenStore).blockJti(eq(jti.toString()), eq(900L));
        assertThat(rt.isRevoked()).isTrue();
    }
}
