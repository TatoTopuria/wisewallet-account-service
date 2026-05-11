package com.wisewallet.account.infrastructure.persistence;

import com.wisewallet.account.domain.model.AccountReservation;
import com.wisewallet.account.domain.model.ReservationStatus;
import com.wisewallet.account.domain.repository.AccountReservationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AccountReservationRepositoryAdapter implements AccountReservationRepositoryPort {

    private final AccountReservationJpaRepository jpa;

    @Override
    public AccountReservation save(AccountReservation reservation) {
        return jpa.save(reservation);
    }

    @Override
    public Optional<AccountReservation> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public List<AccountReservation> findByAccountBalanceIdAndStatus(UUID accountBalanceId, ReservationStatus status) {
        return jpa.findByAccountBalanceIdAndStatus(accountBalanceId, status);
    }

    @Override
    public List<AccountReservation> findExpiredActive(Instant now) {
        return jpa.findExpiredActive(now);
    }

    @Override
    public void saveAll(List<AccountReservation> reservations) {
        jpa.saveAll(reservations);
    }
}
