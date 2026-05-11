package com.wisewallet.account.infrastructure.persistence;

import com.wisewallet.account.domain.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshToken, UUID> {
    @Query("SELECT t FROM RefreshToken t WHERE t.userId = :userId AND t.revoked = false ORDER BY t.createdAt DESC LIMIT 1")
    Optional<RefreshToken> findActiveByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE RefreshToken t SET t.revoked = true WHERE t.userId = :userId")
    void revokeAllForUser(@Param("userId") UUID userId);
}
