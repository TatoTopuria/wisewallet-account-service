package com.wisewallet.account.presentation.dto.response;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String refreshToken
) {
    public static LoginResponse of(String accessToken, String refreshToken) {
        return new LoginResponse(accessToken, "Bearer", 900L, refreshToken);
    }
}
