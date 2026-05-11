package com.wisewallet.account.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wisewallet.account.domain.event.AccountCreatedDomainEvent;
import com.wisewallet.account.domain.event.BalanceLowDomainEvent;
import com.wisewallet.account.domain.model.OutboxEvent;
import com.wisewallet.account.domain.repository.OutboxEventRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Converts domain events to OutboxEvent rows within the current transaction (BEFORE_COMMIT),
 * guaranteeing at-least-once delivery via the outbox pattern.
 */
@Component
@RequiredArgsConstructor
public class DomainEventToOutboxListener {

    private final OutboxEventRepositoryPort outboxRepository;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleAccountCreated(AccountCreatedDomainEvent event) {
        AccountCreatedEvent payload = new AccountCreatedEvent(
                UUID.randomUUID(),
                "ACCOUNT_CREATED",
                Instant.now(),
                event.userId(),
                event.accountId(),
                event.accountType(),
                event.currency(),
                event.nickname()
        );
        outboxRepository.save(buildOutboxEvent("Account", event.accountId(), "account.created", payload));
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleBalanceLow(BalanceLowDomainEvent event) {
        BalanceLowEvent payload = new BalanceLowEvent(
                UUID.randomUUID(),
                "BALANCE_LOW",
                Instant.now(),
                event.userId(),
                event.accountId(),
                event.accountBalanceId(),
                event.currency(),
                event.currentBalance(),
                event.threshold()
        );
        outboxRepository.save(buildOutboxEvent("AccountBalance", event.accountBalanceId(), "account.balance-low", payload));
    }

    private OutboxEvent buildOutboxEvent(String aggregateType, UUID aggregateId, String eventType, Object payload) {
        try {
            return OutboxEvent.builder()
                    .id(UUID.randomUUID())
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(objectMapper.writeValueAsString(payload))
                    .status("PENDING")
                    .build();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize outbox event", e);
        }
    }
}
