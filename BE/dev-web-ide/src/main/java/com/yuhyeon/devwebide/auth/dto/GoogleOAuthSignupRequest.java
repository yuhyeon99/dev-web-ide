package com.yuhyeon.devwebide.auth.dto;

public record GoogleOAuthSignupRequest(
        String nickname,
        boolean termsAgreed,
        boolean privacyAgreed
) {
}
