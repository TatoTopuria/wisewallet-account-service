package com.wisewallet.account.infrastructure.security;

import com.wisewallet.account.application.port.out.TokenIssuerPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtTokenService implements TokenIssuerPort {

    private final JwtProperties jwtProperties;

    public String issueAccessToken(UUID userId, List<String> roles, List<UUID> accountIds, UUID jti) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(jwtProperties.getAccessTokenTtlSeconds());

        return Jwts.builder()
                .id(jti.toString())
                .issuer(jwtProperties.getIssuer())
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("roles", roles)
                .claim("accountIds", accountIds.stream().map(UUID::toString).toList())
                .signWith(signingKey())
                .compact();
    }

    public Claims parseAccessToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new com.wisewallet.account.domain.exception.InvalidTokenException("Invalid or expired JWT: " + e.getMessage());
        }
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public long getAccessTokenTtlSeconds() {
        return jwtProperties.getAccessTokenTtlSeconds();
    }

    @Override
    public long getRefreshTokenTtlDays() {
        return jwtProperties.getRefreshTokenTtlDays();
    }
}
