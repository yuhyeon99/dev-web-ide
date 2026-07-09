package com.yuhyeon.devwebide.auth.dto;

import com.yuhyeon.devwebide.user.domain.User;
import com.yuhyeon.devwebide.user.domain.UserRole;
import com.yuhyeon.devwebide.user.domain.UserStatus;

import java.time.LocalDateTime;

public record OAuthLoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        LocalDateTime accessTokenExpiresAt,
        Long userId,
        String email,
        String nickname,
        UserRole role,
        UserStatus status,
        boolean newUser
) {

    public static OAuthLoginResponse of(
            AuthTokenRefreshResponse tokenResponse,
            User user,
            boolean newUser
    ) {
        return new OAuthLoginResponse(
                tokenResponse.accessToken(),
                tokenResponse.tokenType(),
                tokenResponse.expiresIn(),
                tokenResponse.accessTokenExpiresAt(),
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole(),
                user.getStatus(),
                newUser
        );
    }
}
