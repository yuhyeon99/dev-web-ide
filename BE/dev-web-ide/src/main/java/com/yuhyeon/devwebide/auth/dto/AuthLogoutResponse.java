package com.yuhyeon.devwebide.auth.dto;

import java.time.LocalDateTime;

public record AuthLogoutResponse(
        boolean loggedOut,
        LocalDateTime revokedAt
) {

    public static AuthLogoutResponse of(boolean loggedOut, LocalDateTime revokedAt) {
        return new AuthLogoutResponse(loggedOut, revokedAt);
    }
}
