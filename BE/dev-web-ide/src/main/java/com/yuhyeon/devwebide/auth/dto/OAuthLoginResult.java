package com.yuhyeon.devwebide.auth.dto;

public record OAuthLoginResult(
        OAuthLoginResponse response,
        String refreshToken,
        long refreshTokenExpiresInSeconds
) {
}
