package com.wisewallet.account.application.port.out;

import java.util.List;
import java.util.UUID;

public interface TokenIssuerPort {
    String issueAccessToken(UUID userId, List<String> roles, List<UUID> accountIds, UUID jti);
    long getAccessTokenTtlSeconds();
    long getRefreshTokenTtlDays();
}
