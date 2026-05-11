package com.wisewallet.account.domain.event;

import java.util.UUID;

public record AccountCreatedDomainEvent(
        UUID userId,
        UUID accountId,
        String accountType,
        String currency,
        String nickname
) implements DomainEvent {
}
