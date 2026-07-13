package com.yuhyeon.devwebide.auth.dto;

public record GoogleOAuthUserInfo(
        String subject,
        String email,
        String name
) {
}
