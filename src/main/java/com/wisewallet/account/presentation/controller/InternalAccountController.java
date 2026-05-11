package com.wisewallet.account.presentation.controller;

import com.wisewallet.account.application.command.BalanceCommandService;
import com.wisewallet.account.application.query.AccountQueryService;
import com.wisewallet.account.domain.model.Account;
import com.wisewallet.account.domain.model.AccountBalance;
import com.wisewallet.account.domain.model.AccountReservation;
import com.wisewallet.account.presentation.dto.request.*;
import com.wisewallet.account.presentation.dto.response.InternalAccountInfoResponse;
import com.wisewallet.account.presentation.dto.response.InternalBalanceResponse;
import com.wisewallet.account.presentation.dto.response.ReserveResponse;
import com.wisewallet.account.presentation.mapper.AccountMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/accounts")
@RequiredArgsConstructor
public class InternalAccountController {

    private final BalanceCommandService balanceCommandService;
    private final AccountQueryService accountQueryService;
    private final AccountMapper accountMapper;

    @GetMapping("/{accountId}")
    public InternalAccountInfoResponse getAccount(@PathVariable UUID accountId) {
        Account account = accountQueryService.getAccount(accountId);
        List<AccountBalance> balances = accountQueryService.getBalancesForAccount(accountId);
        List<String> currencies = balances.stream()
                .map(AccountBalance::getCurrency)
                .toList();
        return new InternalAccountInfoResponse(
                account.getId(),
                account.getUser().getId(),
                account.getType().name(),
                account.getStatus().name(),
                currencies
        );
    }

    @PostMapping("/{accountId}/balances/{currency}/debit")
    public InternalBalanceResponse debit(@PathVariable UUID accountId,
                                         @PathVariable String currency,
                                         @Valid @RequestBody InternalDebitRequest request) {
        AccountBalance balance = balanceCommandService.debit(accountId, currency, request.amount(), request.transactionId());
        return accountMapper.toInternalBalanceResponse(balance);
    }

    @PostMapping("/{accountId}/balances/{currency}/credit")
    public InternalBalanceResponse credit(@PathVariable UUID accountId,
                                          @PathVariable String currency,
                                          @Valid @RequestBody InternalCreditRequest request) {
        AccountBalance balance = balanceCommandService.credit(accountId, currency, request.amount(), request.transactionId());
        return accountMapper.toInternalBalanceResponse(balance);
    }

    @PostMapping("/{accountId}/balances/{currency}/reserve")
    public ReserveResponse reserve(@PathVariable UUID accountId,
                                   @PathVariable String currency,
                                   @Valid @RequestBody InternalReserveRequest request) {
        AccountReservation reservation = balanceCommandService.reserve(accountId, currency, request.amount(), request.transactionId());
        AccountBalance balance = reservation.getAccountBalance();
        return new ReserveResponse(
                balance.getAccount().getId(),
                balance.getCurrency(),
                balance.getAmount(),
                balance.getReservedAmount(),
                balance.availableBalance(),
                reservation.getId()
        );
    }

    @PostMapping("/{accountId}/balances/{currency}/commit")
    public InternalBalanceResponse commit(@PathVariable UUID accountId,
                                          @PathVariable String currency,
                                          @Valid @RequestBody InternalCommitRequest request) {
        AccountBalance balance = balanceCommandService.commit(accountId, currency, request.reservationId());
        return accountMapper.toInternalBalanceResponse(balance);
    }

    @PostMapping("/{accountId}/balances/{currency}/release")
    public InternalBalanceResponse release(@PathVariable UUID accountId,
                                           @PathVariable String currency,
                                           @Valid @RequestBody InternalReleaseRequest request) {
        AccountBalance balance = balanceCommandService.release(accountId, currency, request.reservationId());
        return accountMapper.toInternalBalanceResponse(balance);
    }
}
