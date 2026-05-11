package com.wisewallet.account.presentation.dto.response;

import java.util.UUID;

public record RegisterResponse(
        UUID userId,
        String email,
        String message
) {}
