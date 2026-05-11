package com.wisewallet.account.domain.event;

import java.math.BigDecimal;
import java.util.UUID;

public record BalanceLowDomainEvent(
        UUID userId,
        UUID accountId,
        UUID accountBalanceId,
        String currency,
        BigDecimal currentBalance,
        BigDecimal threshold
) implements DomainEvent {
}
