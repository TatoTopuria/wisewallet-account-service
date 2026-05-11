package com.wisewallet.account.infrastructure.persistence;

import com.wisewallet.account.domain.model.Account;
import com.wisewallet.account.domain.model.AccountStatus;
import com.wisewallet.account.domain.repository.AccountRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepositoryPort {

    private final AccountJpaRepository jpa;

    @Override
    public Account save(Account account) {
        return jpa.save(account);
    }

    @Override
    public Optional<Account> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public List<Account> findByUserIdAndStatusNot(UUID userId, AccountStatus excludedStatus) {
        return jpa.findByUserIdAndStatusNot(userId, excludedStatus);
    }

    @Override
    public Optional<Account> findByIdAndUserId(UUID id, UUID userId) {
        return jpa.findByIdAndUserId(id, userId);
    }
}
