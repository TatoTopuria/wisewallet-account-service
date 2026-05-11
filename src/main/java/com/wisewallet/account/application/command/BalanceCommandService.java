package com.wisewallet.account.application.command;

import com.wisewallet.account.domain.event.BalanceLowDomainEvent;
import com.wisewallet.account.domain.exception.AccountNotActiveException;
import com.wisewallet.account.domain.exception.AccountNotFoundException;
import com.wisewallet.account.domain.exception.BusinessRuleException;
import com.wisewallet.account.domain.model.*;
import com.wisewallet.account.domain.repository.*;
import com.wisewallet.account.domain.service.BalanceLowCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceCommandService {

    private final AccountBalanceRepositoryPort balanceRepository;
    private final AccountReservationRepositoryPort reservationRepository;
    private final UserRepositoryPort userRepository;
    private final AccountRepositoryPort accountRepository;
    private final ProcessedBalanceOperationRepositoryPort processedOperationRepository;
    private final BalanceLowCheckService balanceLowCheckService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public AccountBalance debit(UUID accountId, String currency, BigDecimal amount, UUID transactionId) {
        if (!recordOperation(transactionId, accountId, "DEBIT")) {
            log.debug("Duplicate debit detected. transactionId={}, accountId={} — returning current balance",
                    transactionId, accountId);
            return getActiveBalance(accountId, currency);
        }

        AccountBalance balance = getActiveBalanceForUpdate(accountId, currency);

        BigDecimal available = balance.availableBalance();
        if (available.compareTo(amount) < 0) {
            throw new BusinessRuleException("Insufficient funds: available=" + available + " requested=" + amount);
        }

        balance.setAmount(balance.getAmount().subtract(amount));
        AccountBalance saved = balanceRepository.save(balance);

        checkAndPublishBalanceLow(saved);
        return saved;
    }

    @Transactional
    public AccountBalance credit(UUID accountId, String currency, BigDecimal amount, UUID transactionId) {
        if (!recordOperation(transactionId, accountId, "CREDIT")) {
            log.debug("Duplicate credit detected. transactionId={}, accountId={} — returning current balance",
                    transactionId, accountId);
            return getActiveBalance(accountId, currency);
        }

        AccountBalance balance = getActiveBalanceForUpdate(accountId, currency);
        balance.setAmount(balance.getAmount().add(amount));
        return balanceRepository.save(balance);
    }

    @Transactional
    public AccountReservation reserve(UUID accountId, String currency, BigDecimal amount, UUID transactionId) {
        if (!recordOperation(transactionId, accountId, "RESERVE")) {
            log.debug("Duplicate reserve detected. transactionId={}, accountId={} — returning existing reservation",
                    transactionId, accountId);
            // Return the existing reservation for idempotent replay
            return reservationRepository.findByTransactionId(transactionId)
                    .orElseThrow(() -> new BusinessRuleException(
                            "Duplicate reserve: no existing reservation found for transactionId=" + transactionId));
        }

        AccountBalance balance = getActiveBalanceForUpdate(accountId, currency);

        BigDecimal available = balance.availableBalance();
        if (available.compareTo(amount) < 0) {
            throw new BusinessRuleException("Insufficient available balance for reservation: available=" + available + " requested=" + amount);
        }

        balance.setReservedAmount(balance.getReservedAmount().add(amount));
        AccountBalance saved = balanceRepository.save(balance);

        AccountReservation reservation = AccountReservation.builder()
                .id(UUID.randomUUID())
                .accountBalance(saved)
                .transactionId(transactionId)
                .amount(amount)
                .status(ReservationStatus.ACTIVE)
                .expiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
                .build();

        return reservationRepository.save(reservation);
    }

    @Transactional
    public AccountBalance commit(UUID accountId, String currency, UUID reservationId) {
        AccountBalance balance = getActiveBalanceForUpdate(accountId, currency);

        AccountReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessRuleException("Reservation not found: " + reservationId));

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new BusinessRuleException("Reservation is not ACTIVE: status=" + reservation.getStatus());
        }

        balance.setReservedAmount(balance.getReservedAmount().subtract(reservation.getAmount()));
        balance.setAmount(balance.getAmount().subtract(reservation.getAmount()));
        reservation.setStatus(ReservationStatus.COMMITTED);

        reservationRepository.save(reservation);
        AccountBalance saved = balanceRepository.save(balance);

        checkAndPublishBalanceLow(saved);
        return saved;
    }

    @Transactional
    public AccountBalance release(UUID accountId, String currency, UUID reservationId) {
        AccountBalance balance = getActiveBalanceForUpdate(accountId, currency);

        AccountReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessRuleException("Reservation not found: " + reservationId));

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new BusinessRuleException("Reservation is not ACTIVE: status=" + reservation.getStatus());
        }

        balance.setReservedAmount(balance.getReservedAmount().subtract(reservation.getAmount()));
        reservation.setStatus(ReservationStatus.RELEASED);

        reservationRepository.save(reservation);
        return balanceRepository.save(balance);
    }

    // ── private helpers ───────────────────────────────────────────

    /**
     * Inserts a {@link ProcessedBalanceOperation} record to mark this operation as seen.
     * Returns {@code true} if the insert succeeded (first time); {@code false} if the unique
     * constraint fired (duplicate — caller should return idempotent success).
     */
    private boolean recordOperation(UUID transactionId, UUID accountId, String operationType) {
        try {
            processedOperationRepository.save(ProcessedBalanceOperation.builder()
                    .id(UUID.randomUUID())
                    .transactionId(transactionId)
                    .accountId(accountId)
                    .operationType(operationType)
                    .build());
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }

    private AccountBalance getActiveBalanceForUpdate(UUID accountId, String currency) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(accountId);
        }

        return balanceRepository.findByAccountIdAndCurrencyForUpdate(accountId, currency)
                .orElseThrow(() -> new BusinessRuleException(
                        "No balance found for account=" + accountId + " currency=" + currency));
    }

    private AccountBalance getActiveBalance(UUID accountId, String currency) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(accountId);
        }

        return balanceRepository.findByAccountIdAndCurrency(accountId, currency)
                .orElseThrow(() -> new BusinessRuleException(
                        "No balance found for account=" + accountId + " currency=" + currency));
    }

    private void checkAndPublishBalanceLow(AccountBalance balance) {
        User user = userRepository.findById(balance.getAccount().getUser().getId())
                .orElseThrow();

        BigDecimal available = balance.availableBalance();
        if (balanceLowCheckService.isBalanceLow(available, user.getBalanceLowThreshold())) {
            eventPublisher.publishEvent(new BalanceLowDomainEvent(
                    user.getId(),
                    balance.getAccount().getId(),
                    balance.getId(),
                    balance.getCurrency(),
                    available,
                    user.getBalanceLowThreshold()
            ));
        }
    }
}
