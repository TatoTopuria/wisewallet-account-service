package com.wisewallet.account.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wisewallet.account.application.command.AccountCommandService;
import com.wisewallet.account.application.query.AccountQueryService;
import com.wisewallet.account.domain.model.*;
import com.wisewallet.account.presentation.dto.request.CreateAccountRequest;
import com.wisewallet.account.presentation.mapper.AccountMapper;
import com.wisewallet.account.presentation.dto.response.AccountResponse;
import com.wisewallet.account.presentation.dto.response.BalanceResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AccountCommandService accountCommandService;
    @MockBean AccountQueryService accountQueryService;
    @MockBean AccountMapper accountMapper;

    @Test
    void listAccounts_returnsOk() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        Account account = Account.builder().id(accountId).status(AccountStatus.ACTIVE)
                .type(AccountType.CHECKING).version(0L).build();
        AccountResponse response = new AccountResponse(accountId, null, AccountType.CHECKING,
                AccountStatus.ACTIVE, List.of(), null);

        when(accountQueryService.listAccountsForUser(userId)).thenReturn(List.of(account));
        when(accountQueryService.getBalancesForAccount(accountId)).thenReturn(List.of());
        when(accountMapper.toAccountResponse(eq(account), anyList())).thenReturn(response);

        mockMvc.perform(get("/api/accounts")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(accountId.toString()));
    }

    @Test
    void createAccount_validRequest_returns201() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        Account account = Account.builder().id(accountId).status(AccountStatus.ACTIVE)
                .type(AccountType.SAVINGS).version(0L).build();
        AccountResponse response = new AccountResponse(accountId, "My Savings", AccountType.SAVINGS,
                AccountStatus.ACTIVE, List.of(), null);

        when(accountCommandService.createAccount(any(), any(AccountType.class), anyString(), anyString()))
                .thenReturn(account);
        when(accountQueryService.getBalancesForAccount(accountId)).thenReturn(List.of());
        when(accountMapper.toAccountResponse(eq(account), anyList())).thenReturn(response);

        var request = new CreateAccountRequest("My Savings", AccountType.SAVINGS, "USD");

        mockMvc.perform(post("/api/accounts")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(accountId.toString()))
                .andExpect(jsonPath("$.type").value("SAVINGS"));
    }

    @Test
    void listAccounts_missingHeader_returns400() throws Exception {
        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isBadRequest());
    }
}
