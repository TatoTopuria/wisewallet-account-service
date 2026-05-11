package com.wisewallet.account.presentation.dto.response;

import java.math.BigDecimal;

public record BalanceResponse(
        String currency,
        BigDecimal amount,
        BigDecimal reservedAmount,
        BigDecimal availableBalance
) {}
