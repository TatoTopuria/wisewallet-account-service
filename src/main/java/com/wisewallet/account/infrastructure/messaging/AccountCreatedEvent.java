package com.wisewallet.account.infrastructure.messaging;

import com.wisewallet.account.domain.model.AccountBalance;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountCreatedEvent(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        UUID userId,
        UUID accountId,
        String accountType,
        String currency,
        String nickname
) {}
