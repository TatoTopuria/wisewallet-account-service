package com.wisewallet.account.application.command;

import com.wisewallet.account.domain.exception.BusinessRuleException;
import com.wisewallet.account.domain.model.*;
import com.wisewallet.account.domain.repository.*;
import com.wisewallet.account.domain.service.BalanceLowCheckService;
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
class BalanceCommandServiceTest {

    @Mock AccountBalanceRepositoryPort balanceRepository;
    @Mock AccountReservationRepositoryPort reservationRepository;
    @Mock UserRepositoryPort userRepository;
    @Mock AccountRepositoryPort accountRepository;
    @Mock ProcessedBalanceOperationRepositoryPort processedOperationRepository;
    @Mock BalanceLowCheckService balanceLowCheckService;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks
    BalanceCommandService service;

    private Account activeAccount(UUID accountId, UUID userId) {
        User user = User.builder().id(userId)
                .balanceLowThreshold(BigDecimal.valueOf(100)).build();
        return Account.builder().id(accountId).user(user)
                .status(AccountStatus.ACTIVE).version(0L).build();
    }

    private AccountBalance balance(Account account, BigDecimal amount, BigDecimal reserved) {
        return AccountBalance.builder()
                .id(UUID.randomUUID()).account(account)
                .currency("USD").amount(amount).reservedAmount(reserved)
                .version(0L).build();
    }

    @Test
    void debit_sufficientFunds_succeeds() {
        UUID accId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Account account = activeAccount(accId, userId);
        AccountBalance bal = balance(account, BigDecimal.valueOf(200), BigDecimal.ZERO);

        when(accountRepository.findById(accId)).thenReturn(Optional.of(account));
        when(balanceRepository.findByAccountIdAndCurrencyForUpdate(accId, "USD")).thenReturn(Optional.of(bal));
        when(balanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(userId)).thenReturn(Optional.of(account.getUser()));
        when(balanceLowCheckService.isBalanceLow(any(), any())).thenReturn(false);

        AccountBalance result = service.debit(accId, "USD", BigDecimal.valueOf(50), UUID.randomUUID());
        assertThat(result.getAmount()).isEqualByComparingTo("150");
    }

    @Test
    void debit_insufficientFunds_throwsBusinessRuleException() {
        UUID accId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Account account = activeAccount(accId, userId);
        AccountBalance bal = balance(account, BigDecimal.valueOf(30), BigDecimal.ZERO);

        when(accountRepository.findById(accId)).thenReturn(Optional.of(account));
        when(balanceRepository.findByAccountIdAndCurrencyForUpdate(accId, "USD")).thenReturn(Optional.of(bal));

        assertThatThrownBy(() -> service.debit(accId, "USD", BigDecimal.valueOf(50), UUID.randomUUID()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Insufficient");
    }

    @Test
    void debit_belowThreshold_publishesBalanceLowEvent() {
        UUID accId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Account account = activeAccount(accId, userId);
        AccountBalance bal = balance(account, BigDecimal.valueOf(120), BigDecimal.ZERO);

        when(accountRepository.findById(accId)).thenReturn(Optional.of(account));
        when(balanceRepository.findByAccountIdAndCurrencyForUpdate(accId, "USD")).thenReturn(Optional.of(bal));
        when(balanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(userId)).thenReturn(Optional.of(account.getUser()));
        when(balanceLowCheckService.isBalanceLow(any(), any())).thenReturn(true);

        service.debit(accId, "USD", BigDecimal.valueOf(30), UUID.randomUUID());

        verify(eventPublisher).publishEvent(any(com.wisewallet.account.domain.event.BalanceLowDomainEvent.class));
    }
}
