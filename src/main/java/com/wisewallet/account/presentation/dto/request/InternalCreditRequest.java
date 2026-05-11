package com.wisewallet.account.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record InternalCreditRequest(
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotNull UUID transactionId
) {}
