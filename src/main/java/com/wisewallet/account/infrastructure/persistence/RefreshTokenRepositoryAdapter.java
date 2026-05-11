package com.wisewallet.account.infrastructure.persistence;

import com.wisewallet.account.domain.model.RefreshToken;
import com.wisewallet.account.domain.repository.RefreshTokenRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort {

    private final RefreshTokenJpaRepository jpa;

    @Override
    public RefreshToken save(RefreshToken token) {
        return jpa.save(token);
    }

    @Override
    public Optional<RefreshToken> findActiveByUserId(UUID userId) {
        return jpa.findActiveByUserId(userId);
    }

    @Override
    public void revokeAllForUser(UUID userId) {
        jpa.revokeAllForUser(userId);
    }
}
