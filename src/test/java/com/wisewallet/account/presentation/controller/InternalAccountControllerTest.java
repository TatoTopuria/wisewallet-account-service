package com.wisewallet.account.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wisewallet.account.application.command.BalanceCommandService;
import com.wisewallet.account.application.query.AccountQueryService;
import com.wisewallet.account.domain.exception.BusinessRuleException;
import com.wisewallet.account.domain.model.*;
import com.wisewallet.account.presentation.dto.request.InternalDebitRequest;
import com.wisewallet.account.presentation.dto.request.InternalCreditRequest;
import com.wisewallet.account.presentation.mapper.AccountMapper;
import com.wisewallet.account.presentation.dto.response.InternalBalanceResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InternalAccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class InternalAccountControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean BalanceCommandService balanceCommandService;
    @MockBean AccountQueryService accountQueryService;
    @MockBean AccountMapper accountMapper;

    @Test
    void debit_sufficientFunds_returns200() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();
        AccountBalance balance = AccountBalance.builder()
                .id(UUID.randomUUID())
                .currency("USD")
                .amount(BigDecimal.valueOf(150))
                .reservedAmount(BigDecimal.ZERO)
                .version(1L)
                .build();
        InternalBalanceResponse resp = new InternalBalanceResponse(
                accountId, "USD", BigDecimal.valueOf(150), BigDecimal.ZERO, BigDecimal.valueOf(150));

        when(balanceCommandService.debit(eq(accountId), eq("USD"), any(), any())).thenReturn(balance);
        when(accountMapper.toInternalBalanceResponse(balance)).thenReturn(resp);

        var request = new InternalDebitRequest(BigDecimal.valueOf(50), txId);

        mockMvc.perform(post("/internal/accounts/{id}/balances/USD/debit", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.balance").value(150));
    }

    @Test
    void debit_insufficientFunds_returns422() throws Exception {
        UUID accountId = UUID.randomUUID();
        when(balanceCommandService.debit(any(), any(), any(), any()))
                .thenThrow(new BusinessRuleException("Insufficient funds"));

        var request = new InternalDebitRequest(BigDecimal.valueOf(9999), UUID.randomUUID());

        mockMvc.perform(post("/internal/accounts/{id}/balances/USD/debit", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void credit_validRequest_returns200() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();
        AccountBalance balance = AccountBalance.builder()
                .id(UUID.randomUUID()).currency("USD")
                .amount(BigDecimal.valueOf(350)).reservedAmount(BigDecimal.ZERO).version(1L).build();
        InternalBalanceResponse resp = new InternalBalanceResponse(
                accountId, "USD", BigDecimal.valueOf(350), BigDecimal.ZERO, BigDecimal.valueOf(350));

        when(balanceCommandService.credit(eq(accountId), eq("USD"), any(), any())).thenReturn(balance);
        when(accountMapper.toInternalBalanceResponse(balance)).thenReturn(resp);

        var request = new InternalCreditRequest(BigDecimal.valueOf(100), txId);

        mockMvc.perform(post("/internal/accounts/{id}/balances/USD/credit", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(350));
    }
}
