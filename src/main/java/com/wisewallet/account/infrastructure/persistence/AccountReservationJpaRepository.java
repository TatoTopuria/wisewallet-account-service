package com.wisewallet.account.infrastructure.persistence;

import com.wisewallet.account.domain.model.AccountReservation;
import com.wisewallet.account.domain.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AccountReservationJpaRepository extends JpaRepository<AccountReservation, UUID> {
    List<AccountReservation> findByAccountBalanceIdAndStatus(UUID accountBalanceId, ReservationStatus status);

    @Query("SELECT r FROM AccountReservation r WHERE r.status = 'ACTIVE' AND r.expiresAt < :now")
    List<AccountReservation> findExpiredActive(@Param("now") Instant now);
}
