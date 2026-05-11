package com.wisewallet.account.application.query;

import com.wisewallet.account.domain.exception.AccountNotFoundException;
import com.wisewallet.account.domain.exception.UserNotFoundException;
import com.wisewallet.account.domain.model.Account;
import com.wisewallet.account.domain.model.AccountBalance;
import com.wisewallet.account.domain.model.AccountStatus;
import com.wisewallet.account.domain.model.User;
import com.wisewallet.account.domain.repository.AccountBalanceRepositoryPort;
import com.wisewallet.account.domain.repository.AccountRepositoryPort;
import com.wisewallet.account.domain.repository.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountQueryService {

    private final AccountRepositoryPort accountRepository;
    private final AccountBalanceRepositoryPort balanceRepository;
    private final UserRepositoryPort userRepository;

    @Transactional(readOnly = true)
    public List<Account> listAccountsForUser(UUID userId) {
        return accountRepository.findByUserIdAndStatusNot(userId, AccountStatus.CLOSED);
    }

    @Transactional(readOnly = true)
    public Account getAccount(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    @Transactional(readOnly = true)
    public Account getAccountForUser(UUID accountId, UUID userId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    @Transactional(readOnly = true)
    public List<AccountBalance> getBalancesForAccount(UUID accountId) {
        return balanceRepository.findByAccountId(accountId);
    }

    @Transactional(readOnly = true)
    public User getUserInfo(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }
}
