package com.yuhyeon.devwebide.auth.dto;

import com.yuhyeon.devwebide.user.domain.OAuthProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OAuthLoginRequest(
        @NotNull(message = "OAuth provider는 필수입니다.")
        OAuthProvider provider,

        @NotBlank(message = "OAuth provider 사용자 ID는 필수입니다.")
        String providerUserId,

        @NotBlank(message = "OAuth provider 이메일은 필수입니다.")
        String providerEmail,

        @NotBlank(message = "닉네임은 필수입니다.")
        String nickname,

        boolean termsAgreed,

        boolean privacyAgreed
) {
}
