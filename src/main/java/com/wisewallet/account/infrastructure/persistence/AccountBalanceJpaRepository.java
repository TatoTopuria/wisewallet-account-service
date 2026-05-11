package com.wisewallet.account.infrastructure.persistence;

import com.wisewallet.account.domain.model.AccountBalance;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountBalanceJpaRepository extends JpaRepository<AccountBalance, UUID> {
    Optional<AccountBalance> findByAccountIdAndCurrency(UUID accountId, String currency);
    List<AccountBalance> findByAccountId(UUID accountId);
    boolean existsByAccountIdAndCurrency(UUID accountId, String currency);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM AccountBalance b WHERE b.account.id = :accountId AND b.currency = :currency")
    Optional<AccountBalance> findByAccountIdAndCurrencyForUpdate(
            @Param("accountId") UUID accountId,
            @Param("currency") String currency);
}
