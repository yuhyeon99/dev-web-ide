package com.yuhyeon.devwebide.auth.dto;

import java.time.LocalDateTime;

public record AuthTokenRefreshResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        LocalDateTime accessTokenExpiresAt
) {

    private static final String TOKEN_TYPE = "Bearer";

    public static AuthTokenRefreshResponse of(
            String accessToken,
            long expiresIn,
            LocalDateTime accessTokenExpiresAt
    ) {
        return new AuthTokenRefreshResponse(
                accessToken,
                TOKEN_TYPE,
                expiresIn,
                accessTokenExpiresAt
        );
    }
}
