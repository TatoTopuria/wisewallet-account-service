package com.wisewallet.account.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InternalCommitRequest(
        @NotNull UUID reservationId
) {}
