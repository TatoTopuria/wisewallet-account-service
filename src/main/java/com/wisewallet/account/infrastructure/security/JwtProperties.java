package com.wisewallet.account.infrastructure.security;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

    private String secret;
    private String issuer = "wisewallet";
    private long accessTokenTtlSeconds = 900;
    private long refreshTokenTtlDays = 7;

    @PostConstruct
    public void validate() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                    "jwt.secret must be at least 32 characters. Current length: " +
                    (secret == null ? 0 : secret.length()));
        }
    }
}
