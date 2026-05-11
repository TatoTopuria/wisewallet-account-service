package com.wisewallet.account.infrastructure.persistence;

import com.wisewallet.account.domain.model.ProcessedBalanceOperation;
import com.wisewallet.account.domain.repository.ProcessedBalanceOperationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProcessedBalanceOperationRepositoryAdapter implements ProcessedBalanceOperationRepositoryPort {

    private final ProcessedBalanceOperationJpaRepository jpaRepository;

    @Override
    public ProcessedBalanceOperation save(ProcessedBalanceOperation operation) {
        return jpaRepository.save(operation);
    }
}
