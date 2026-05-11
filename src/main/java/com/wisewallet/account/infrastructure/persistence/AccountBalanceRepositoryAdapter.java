package com.wisewallet.account.infrastructure.persistence;

import com.wisewallet.account.domain.model.AccountBalance;
import com.wisewallet.account.domain.repository.AccountBalanceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AccountBalanceRepositoryAdapter implements AccountBalanceRepositoryPort {

    private final AccountBalanceJpaRepository jpa;

    @Override
    public AccountBalance save(AccountBalance balance) {
        return jpa.save(balance);
    }

    @Override
    public Optional<AccountBalance> findByAccountIdAndCurrency(UUID accountId, String currency) {
        return jpa.findByAccountIdAndCurrency(accountId, currency);
    }

    @Override
    public Optional<AccountBalance> findByAccountIdAndCurrencyForUpdate(UUID accountId, String currency) {
        return jpa.findByAccountIdAndCurrencyForUpdate(accountId, currency);
    }

    @Override
    public Optional<AccountBalance> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public List<AccountBalance> findByAccountId(UUID accountId) {
        return jpa.findByAccountId(accountId);
    }

    @Override
    public boolean existsByAccountIdAndCurrency(UUID accountId, String currency) {
        return jpa.existsByAccountIdAndCurrency(accountId, currency);
    }
}
