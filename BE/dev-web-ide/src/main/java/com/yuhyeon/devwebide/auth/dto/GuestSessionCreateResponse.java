package com.yuhyeon.devwebide.auth.dto;

import com.yuhyeon.devwebide.user.domain.GuestSession;

import java.time.LocalDateTime;

public record GuestSessionCreateResponse(
        Long guestSessionId,
        String guestToken,
        String accessToken,
        String tokenType,
        long expiresIn,
        LocalDateTime accessTokenExpiresAt,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {

    public static GuestSessionCreateResponse of(
            GuestSession guestSession,
            AuthTokenRefreshResponse tokenResponse
    ) {
        return new GuestSessionCreateResponse(
                guestSession.getId(),
                guestSession.getGuestToken(),
                tokenResponse.accessToken(),
                tokenResponse.tokenType(),
                tokenResponse.expiresIn(),
                tokenResponse.accessTokenExpiresAt(),
                guestSession.getExpiresAt(),
                guestSession.getCreatedAt()
        );
    }
}
