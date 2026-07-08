package com.yuhyeon.devwebide.auth.dto;

import com.yuhyeon.devwebide.user.domain.GuestSession;

import java.time.LocalDateTime;

public record GuestSessionCreateResponse(
        Long guestSessionId,
        String guestToken,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {

    public static GuestSessionCreateResponse from(GuestSession guestSession) {
        return new GuestSessionCreateResponse(
                guestSession.getId(),
                guestSession.getGuestToken(),
                guestSession.getExpiresAt(),
                guestSession.getCreatedAt()
        );
    }
}
