package com.wisewallet.account.presentation.dto.request;

import com.wisewallet.account.domain.model.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(
        @Size(max = 100) String nickname,
        @NotNull AccountType type,
        @NotBlank @Pattern(regexp = "^(USD|AED|GEL)$") String currency
) {}
