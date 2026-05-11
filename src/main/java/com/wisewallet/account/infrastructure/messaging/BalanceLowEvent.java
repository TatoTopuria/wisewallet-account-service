package com.wisewallet.account.infrastructure.messaging;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BalanceLowEvent(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        UUID userId,
        UUID accountId,
        UUID accountBalanceId,
        String currency,
        BigDecimal currentBalance,
        BigDecimal threshold
) {}
