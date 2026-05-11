package com.wisewallet.account.infrastructure.persistence;

import com.wisewallet.account.domain.model.AccountBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountBalanceJpaRepository extends JpaRepository<AccountBalance, UUID> {
    Optional<AccountBalance> findByAccountIdAndCurrency(UUID accountId, String currency);
    List<AccountBalance> findByAccountId(UUID accountId);
    boolean existsByAccountIdAndCurrency(UUID accountId, String currency);
}
