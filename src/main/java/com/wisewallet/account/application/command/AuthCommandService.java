package com.wisewallet.account.application.command;

import com.wisewallet.account.domain.event.AccountCreatedDomainEvent;
import com.wisewallet.account.domain.exception.*;
import com.wisewallet.account.domain.model.*;
import com.wisewallet.account.domain.repository.*;
import com.wisewallet.account.application.port.out.EmailPort;
import com.wisewallet.account.application.port.out.TokenIssuerPort;
import com.wisewallet.account.application.port.out.TokenStorePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthCommandService {

    private final UserRepositoryPort userRepository;
    private final AccountRepositoryPort accountRepository;
    private final AccountBalanceRepositoryPort balanceRepository;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final EmailVerificationTokenRepositoryPort verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuerPort tokenIssuer;
    private final TokenStorePort tokenStore;
    private final EmailPort emailPort;
    private final ApplicationEventPublisher eventPublisher;

    public record RegisterResult(UUID userId, String email) {}
    public record LoginResult(String accessToken, String refreshToken) {}

    @Transactional
    public RegisterResult register(String email, String password, String firstName, String lastName) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(email);
        }

        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .status(UserStatus.PENDING_VERIFICATION)
                .balanceLowThreshold(new BigDecimal("100.0000"))
                .roles(Set.of("ROLE_USER"))
                .build();
        userRepository.save(user);

        // Default CHECKING account + USD balance
        UUID accountId = UUID.randomUUID();
        Account account = Account.builder()
                .id(accountId)
                .user(user)
                .type(AccountType.CHECKING)
                .status(AccountStatus.ACTIVE)
                .version(0L)
                .build();
        accountRepository.save(account);

        AccountBalance balance = AccountBalance.builder()
                .id(UUID.randomUUID())
                .account(account)
                .currency("USD")
                .amount(BigDecimal.ZERO)
                .reservedAmount(BigDecimal.ZERO)
                .version(0L)
                .build();
        balanceRepository.save(balance);

        // Email verification token (24h TTL)
        String tokenValue = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .token(tokenValue)
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .build();
        verificationTokenRepository.save(verificationToken);

        // Domain event → outbox
        eventPublisher.publishEvent(new AccountCreatedDomainEvent(userId, accountId, "CHECKING", "USD", null));

        // Email (stubbed for local)
        emailPort.sendVerification(email, tokenValue, firstName);

        return new RegisterResult(userId, email);
    }

    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken vt = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Verification token not found or expired"));

        if (vt.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException("Verification token has expired");
        }

        User user = userRepository.findById(vt.getUserId())
                .orElseThrow(() -> new UserNotFoundException(vt.getUserId()));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        verificationTokenRepository.delete(vt);
    }

    @Transactional
    public LoginResult login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
            throw new AccountNotActiveException("Email not yet verified");
        }
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new AccountNotActiveException("Account is suspended");
        }

        return issueTokenPair(user);
    }

    @Transactional
    public LoginResult refresh(String rawRefreshToken) {
        // Find user by token hash — we need to check all non-expired tokens for this user
        // Strategy: load latest active token, BCrypt match, validate
        // For efficiency we rely on user providing the raw token; we must find active records
        // and BCrypt-check each (only one should be active per user at a time)
        // However, we don't know the userId from the raw token, so we need a different approach.
        // We store the token in a lookup-friendly way: look up by searching with hashed token is not
        // feasible. Instead, the token format embeds userId: <userId>:<randomUUID>
        // Parse userId from token prefix:
        int sep = rawRefreshToken.indexOf(':');
        if (sep < 0) {
            throw new InvalidTokenException("Invalid refresh token format");
        }
        UUID userId;
        try {
            userId = UUID.fromString(rawRefreshToken.substring(0, sep));
        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        RefreshToken stored = refreshTokenRepository.findActiveByUserId(userId)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found or revoked"));

        if (!passwordEncoder.matches(rawRefreshToken, stored.getTokenHash())) {
            throw new InvalidTokenException("Invalid refresh token");
        }
        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException("Refresh token expired or revoked");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return issueTokenPair(user);
    }

    @Transactional
    public void logout(UUID userId) {
        refreshTokenRepository.findActiveByUserId(userId).ifPresent(rt -> {
            tokenStore.blockJti(rt.getAccessJti().toString(), tokenIssuer.getAccessTokenTtlSeconds());
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    // ── private helpers ───────────────────────────────────────────

    private LoginResult issueTokenPair(User user) {
        List<UUID> accountIds = accountRepository
                .findByUserIdAndStatusNot(user.getId(), AccountStatus.CLOSED)
                .stream()
                .map(Account::getId)
                .collect(Collectors.toList());

        UUID jti = UUID.randomUUID();
        String accessToken = tokenIssuer.issueAccessToken(
                user.getId(),
                List.copyOf(user.getRoles()),
                accountIds,
                jti
        );

        // Revoke all previous refresh tokens
        refreshTokenRepository.revokeAllForUser(user.getId());

        // New refresh token: <userId>:<randomUUID>
        String rawToken = user.getId() + ":" + UUID.randomUUID();
        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .tokenHash(passwordEncoder.encode(rawToken))
                .accessJti(jti)
                .expiresAt(Instant.now().plus(tokenIssuer.getRefreshTokenTtlDays(), ChronoUnit.DAYS))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        return new LoginResult(accessToken, rawToken);
    }
}
