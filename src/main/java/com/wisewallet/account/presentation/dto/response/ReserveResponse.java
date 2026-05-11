package com.wisewallet.account.presentation.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ReserveResponse(
        UUID accountId,
        String currency,
        BigDecimal balance,
        BigDecimal reservedAmount,
        BigDecimal availableBalance,
        UUID reservationId
) {}
