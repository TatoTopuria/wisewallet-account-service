package com.wisewallet.account.domain.repository;

import com.wisewallet.account.domain.model.Account;
import com.wisewallet.account.domain.model.AccountStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepositoryPort {
    Account save(Account account);
    Optional<Account> findById(UUID id);
    List<Account> findByUserIdAndStatusNot(UUID userId, AccountStatus excludedStatus);
    Optional<Account> findByIdAndUserId(UUID id, UUID userId);
}
