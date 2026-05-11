package com.wisewallet.account.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Records a balance-mutating operation (debit, credit, reserve) so that
 * duplicate calls with the same (transactionId, accountId, operationType)
 * tuple are silently ignored via a database unique constraint.
 */
@Entity
@Table(
        name = "processed_balance_operations",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_processed_balance_op",
                columnNames = {"transaction_id", "account_id", "operation_type"}
        )
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedBalanceOperation {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "transaction_id", nullable = false, updatable = false)
    private UUID transactionId;

    @Column(name = "account_id", nullable = false, updatable = false)
    private UUID accountId;

    @Column(name = "operation_type", nullable = false, length = 10, updatable = false)
    private String operationType;

    @CreationTimestamp
    @Column(name = "processed_at", updatable = false, nullable = false)
    private Instant processedAt;
}
