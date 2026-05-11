package com.wisewallet.account.infrastructure.persistence;

import com.wisewallet.account.domain.model.Account;
import com.wisewallet.account.domain.model.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountJpaRepository extends JpaRepository<Account, UUID> {
    List<Account> findByUserIdAndStatusNot(UUID userId, AccountStatus excludedStatus);
    Optional<Account> findByIdAndUserId(UUID id, UUID userId);

    // Needed for login/refresh claims
    default UUID getUserIdFromAccount(Account account) {
        return account.getUser().getId();
    }
}
