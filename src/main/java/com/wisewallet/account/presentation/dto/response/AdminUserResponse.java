package com.wisewallet.account.presentation.dto.response;

import com.wisewallet.account.domain.model.UserStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AdminUserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        UserStatus status,
        List<String> roles,
        Instant createdAt
) {}
