package com.wisewallet.account.infrastructure.persistence;

import com.wisewallet.account.domain.model.OutboxEvent;
import com.wisewallet.account.domain.repository.OutboxEventRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OutboxEventRepositoryAdapter implements OutboxEventRepositoryPort {

    private final OutboxEventJpaRepository jpa;

    @Override
    public OutboxEvent save(OutboxEvent event) {
        return jpa.save(event);
    }

    @Override
    public List<OutboxEvent> findPendingOrderedByCreatedAt(int limit) {
        return jpa.findPendingOrderedByCreatedAt(limit);
    }

    @Override
    public void saveAll(List<OutboxEvent> events) {
        jpa.saveAll(events);
    }
}
