package com.wisewallet.account.infrastructure.security;

import com.wisewallet.account.domain.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenServiceTest {

    private JwtTokenService service;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret("test-secret-key-at-least-32-chars!!");
        props.setIssuer("wisewallet");
        props.setAccessTokenTtlSeconds(900);
        service = new JwtTokenService(props);
    }

    @Test
    void issueAndParseRoundTrip() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID jti = UUID.randomUUID();

        String token = service.issueAccessToken(userId, List.of("ROLE_USER"), List.of(accountId), jti);
        assertThat(token).isNotBlank();

        Claims claims = service.parseAccessToken(token);
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.getId()).isEqualTo(jti.toString());
        assertThat(claims.get("roles", List.class)).contains("ROLE_USER");
        assertThat(claims.get("accountIds", List.class)).contains(accountId.toString());
    }

    @Test
    void invalidToken_throwsInvalidTokenException() {
        assertThatThrownBy(() -> service.parseAccessToken("not.a.jwt"))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void wrongSignature_throwsInvalidTokenException() {
        UUID userId = UUID.randomUUID();
        UUID jti = UUID.randomUUID();
        String token = service.issueAccessToken(userId, List.of(), List.of(), jti);
        // tamper with payload
        String[] parts = token.split("\\.");
        String tampered = parts[0] + "." + parts[1] + ".invalidsignature";
        assertThatThrownBy(() -> service.parseAccessToken(tampered))
                .isInstanceOf(InvalidTokenException.class);
    }
}
