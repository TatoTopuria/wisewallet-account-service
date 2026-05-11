package com.wisewallet.account.presentation.controller;

import com.wisewallet.account.application.command.AccountCommandService;
import com.wisewallet.account.application.query.AccountQueryService;
import com.wisewallet.account.domain.model.Account;
import com.wisewallet.account.domain.model.AccountBalance;
import com.wisewallet.account.presentation.dto.request.CreateAccountRequest;
import com.wisewallet.account.presentation.dto.response.AccountResponse;
import com.wisewallet.account.presentation.mapper.AccountMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountCommandService accountCommandService;
    private final AccountQueryService accountQueryService;
    private final AccountMapper accountMapper;

    @GetMapping
    public List<AccountResponse> listAccounts(@RequestHeader("X-User-Id") String userId) {
        UUID uid = UUID.fromString(userId);
        List<Account> accounts = accountQueryService.listAccountsForUser(uid);
        return accounts.stream()
                .map(account -> {
                    List<AccountBalance> balances = accountQueryService.getBalancesForAccount(account.getId());
                    return accountMapper.toAccountResponse(account, balances);
                })
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount(@RequestHeader("X-User-Id") String userId,
                                         @Valid @RequestBody CreateAccountRequest request) {
        UUID uid = UUID.fromString(userId);
        Account account = accountCommandService.createAccount(uid, request.type(), request.currency(), request.nickname());
        List<AccountBalance> balances = accountQueryService.getBalancesForAccount(account.getId());
        return accountMapper.toAccountResponse(account, balances);
    }
}
