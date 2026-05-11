package com.wisewallet.account.domain.repository;

import com.wisewallet.account.domain.model.RefreshToken;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepositoryPort {
    RefreshToken save(RefreshToken token);
    Optional<RefreshToken> findActiveByUserId(UUID userId);
    void revokeAllForUser(UUID userId);
}
