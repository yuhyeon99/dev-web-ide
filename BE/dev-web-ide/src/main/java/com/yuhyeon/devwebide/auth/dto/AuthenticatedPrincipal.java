package com.yuhyeon.devwebide.auth.dto;

public record AuthenticatedPrincipal(
        Long sessionId,
        String sessionType,
        Long userId,
        Long guestSessionId,
        String role
) {

    private static final String USER_SESSION_TYPE = "USER";
    private static final String GUEST_SESSION_TYPE = "GUEST";

    public boolean isUserSession() {
        return USER_SESSION_TYPE.equals(sessionType);
    }

    public boolean isGuestSession() {
        return GUEST_SESSION_TYPE.equals(sessionType);
    }
}
