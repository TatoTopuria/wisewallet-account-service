package com.wisewallet.account.domain.repository;

import com.wisewallet.account.domain.model.ProcessedBalanceOperation;

public interface ProcessedBalanceOperationRepositoryPort {
    ProcessedBalanceOperation save(ProcessedBalanceOperation operation);
}
