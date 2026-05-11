package com.wisewallet.account.infrastructure.persistence;

import com.wisewallet.account.domain.model.ProcessedBalanceOperation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface ProcessedBalanceOperationJpaRepository extends JpaRepository<ProcessedBalanceOperation, UUID> {
}
