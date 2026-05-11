package com.wisewallet.account.presentation.dto.response;

import com.wisewallet.account.domain.model.AccountStatus;
import com.wisewallet.account.domain.model.AccountType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String nickname,
        AccountType type,
        AccountStatus status,
        List<BalanceResponse> balances,
        Instant createdAt
) {}
