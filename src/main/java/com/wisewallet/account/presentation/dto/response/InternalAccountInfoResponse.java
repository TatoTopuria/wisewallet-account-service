package com.wisewallet.account.presentation.dto.response;

import java.util.List;
import java.util.UUID;

public record InternalAccountInfoResponse(
        UUID id,
        UUID userId,
        String accountType,
        String status,
        List<String> availableCurrencies
) {}
