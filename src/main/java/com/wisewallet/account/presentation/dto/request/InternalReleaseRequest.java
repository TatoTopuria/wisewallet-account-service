package com.wisewallet.account.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InternalReleaseRequest(
        @NotNull UUID reservationId
) {}
