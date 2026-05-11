package com.wisewallet.account.presentation.dto.response;

import java.util.UUID;

public record InternalUserInfoResponse(
        UUID userId,
        String email,
        String firstName,
        String lastName
) {}
