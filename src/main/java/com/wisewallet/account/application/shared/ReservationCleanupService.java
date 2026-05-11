package com.wisewallet.account.application.shared;

import com.wisewallet.account.domain.model.AccountReservation;
import com.wisewallet.account.domain.model.ReservationStatus;
import com.wisewallet.account.domain.repository.AccountBalanceRepositoryPort;
import com.wisewallet.account.domain.repository.AccountReservationRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationCleanupService {

    private final AccountReservationRepositoryPort reservationRepository;
    private final AccountBalanceRepositoryPort balanceRepository;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void releaseExpiredReservations() {
        List<AccountReservation> expired = reservationRepository.findExpiredActive(Instant.now());
        if (expired.isEmpty()) {
            return;
        }
        log.warn("Releasing {} expired reservations — Transaction Service may have failed to commit/release",
                expired.size());

        for (AccountReservation reservation : expired) {
            var balance = reservation.getAccountBalance();
            balance.setReservedAmount(balance.getReservedAmount().subtract(reservation.getAmount()));
            balanceRepository.save(balance);
            reservation.setStatus(ReservationStatus.RELEASED);
        }
        reservationRepository.saveAll(expired);
    }
}
