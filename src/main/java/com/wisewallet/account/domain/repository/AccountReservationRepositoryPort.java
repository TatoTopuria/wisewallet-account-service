package com.wisewallet.account.domain.repository;

import com.wisewallet.account.domain.model.AccountReservation;
import com.wisewallet.account.domain.model.ReservationStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountReservationRepositoryPort {
    AccountReservation save(AccountReservation reservation);
    Optional<AccountReservation> findById(UUID id);
    Optional<AccountReservation> findByTransactionId(UUID transactionId);
    List<AccountReservation> findByAccountBalanceIdAndStatus(UUID accountBalanceId, ReservationStatus status);
    List<AccountReservation> findExpiredActive(Instant now);
    void saveAll(List<AccountReservation> reservations);
}
