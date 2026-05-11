package com.wisewallet.account.domain.repository;

import com.wisewallet.account.domain.model.AccountBalance;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountBalanceRepositoryPort {
    AccountBalance save(AccountBalance balance);
    Optional<AccountBalance> findByAccountIdAndCurrency(UUID accountId, String currency);
    Optional<AccountBalance> findByAccountIdAndCurrencyForUpdate(UUID accountId, String currency);
    Optional<AccountBalance> findById(UUID id);
    List<AccountBalance> findByAccountId(UUID accountId);
    boolean existsByAccountIdAndCurrency(UUID accountId, String currency);
}
