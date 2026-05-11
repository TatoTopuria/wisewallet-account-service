package com.wisewallet.account.application.command;

import com.wisewallet.account.domain.event.AccountCreatedDomainEvent;
import com.wisewallet.account.domain.exception.AccountNotActiveException;
import com.wisewallet.account.domain.exception.AccountNotFoundException;
import com.wisewallet.account.domain.exception.BusinessRuleException;
import com.wisewallet.account.domain.model.*;
import com.wisewallet.account.domain.repository.AccountBalanceRepositoryPort;
import com.wisewallet.account.domain.repository.AccountRepositoryPort;
import com.wisewallet.account.domain.repository.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountCommandService {

    private final AccountRepositoryPort accountRepository;
    private final AccountBalanceRepositoryPort balanceRepository;
    private final UserRepositoryPort userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Account createAccount(UUID userId, AccountType type, String currency, String nickname) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.wisewallet.account.domain.exception.UserNotFoundException(userId));

        if (balanceRepository.existsByAccountIdAndCurrency(userId, currency)) {
            // Check is account-level, not user-level; need to find if user already has an account with this currency
            // This check will be done per-account in service; multi accounts allowed
        }

        UUID accountId = UUID.randomUUID();
        Account account = Account.builder()
                .id(accountId)
                .user(user)
                .nickname(nickname)
                .type(type)
                .status(AccountStatus.ACTIVE)
                .version(0L)
                .build();
        accountRepository.save(account);

        if (balanceRepository.existsByAccountIdAndCurrency(accountId, currency)) {
            throw new BusinessRuleException("Balance already exists for currency: " + currency);
        }

        AccountBalance balance = AccountBalance.builder()
                .id(UUID.randomUUID())
                .account(account)
                .currency(currency)
                .amount(BigDecimal.ZERO)
                .reservedAmount(BigDecimal.ZERO)
                .version(0L)
                .build();
        balanceRepository.save(balance);

        eventPublisher.publishEvent(new AccountCreatedDomainEvent(userId, accountId, type.name(), currency, nickname));

        return account;
    }

    @Transactional
    public Account updateStatus(UUID accountId, UUID userId, AccountStatus newStatus) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new AccountNotActiveException("Cannot change status of a closed account");
        }

        account.setStatus(newStatus);
        return accountRepository.save(account);
    }
}
