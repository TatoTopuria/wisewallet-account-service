package com.wisewallet.account.domain.event;

public sealed interface DomainEvent permits AccountCreatedDomainEvent, BalanceLowDomainEvent {
}
