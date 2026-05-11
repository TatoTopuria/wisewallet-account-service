package com.wisewallet.account.application.command;

import com.wisewallet.account.domain.exception.AccountNotActiveException;
import com.wisewallet.account.domain.model.*;
import com.wisewallet.account.domain.repository.AccountBalanceRepositoryPort;
import com.wisewallet.account.domain.repository.AccountRepositoryPort;
import com.wisewallet.account.domain.repository.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountCommandServiceTest {

    @Mock AccountRepositoryPort accountRepository;
    @Mock AccountBalanceRepositoryPort balanceRepository;
    @Mock UserRepositoryPort userRepository;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks
    AccountCommandService service;

    @Test
    void createAccount_forActiveUser_savesAccountAndBalance() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).status(UserStatus.ACTIVE)
                .balanceLowThreshold(BigDecimal.valueOf(100)).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(balanceRepository.existsByAccountIdAndCurrency(any(), any())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(balanceRepository.save(any(AccountBalance.class))).thenAnswer(inv -> inv.getArgument(0));

        Account result = service.createAccount(userId, AccountType.CHECKING, "USD", "My Checking");

        assertThat(result).isNotNull();
        assertThat(result.getUser().getId()).isEqualTo(userId);
        assertThat(result.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        verify(accountRepository).save(any(Account.class));
        verify(balanceRepository).save(any(AccountBalance.class));
        verify(eventPublisher).publishEvent((Object) any());
    }

    @Test
    void updateStatus_toClosed_succeeds() {
        UUID accountId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        Account account = Account.builder()
                .id(accountId).user(user)
                .status(AccountStatus.ACTIVE).version(0L).build();

        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(account));
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.updateStatus(accountId, userId, AccountStatus.CLOSED);

        assertThat(account.getStatus()).isEqualTo(AccountStatus.CLOSED);
        verify(accountRepository).save(account);
    }

    @Test
    void updateStatus_onClosedAccount_throwsAccountNotActiveException() {
        UUID accountId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        Account account = Account.builder()
                .id(accountId).user(user)
                .status(AccountStatus.CLOSED).version(0L).build();

        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> service.updateStatus(accountId, userId, AccountStatus.ACTIVE))
                .isInstanceOf(AccountNotActiveException.class);
    }
}
