package com.wisewallet.account.domain.repository;

import com.wisewallet.account.domain.model.OutboxEvent;

import java.util.List;

public interface OutboxEventRepositoryPort {
    OutboxEvent save(OutboxEvent event);
    List<OutboxEvent> findPendingOrderedByCreatedAt(int limit);
    void saveAll(List<OutboxEvent> events);
}
