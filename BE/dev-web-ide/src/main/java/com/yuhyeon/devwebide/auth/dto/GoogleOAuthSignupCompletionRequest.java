package com.yuhyeon.devwebide.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;

public record GoogleOAuthSignupCompletionRequest(
        @NotBlank(message = "닉네임은 필수입니다.")
        String nickname,

        @AssertTrue(message = "이용약관 동의는 필수입니다.")
        boolean termsAgreed,

        @AssertTrue(message = "개인정보 처리방침 동의는 필수입니다.")
        boolean privacyAgreed
) {

    public GoogleOAuthSignupRequest toSignupRequest() {
        return new GoogleOAuthSignupRequest(
                nickname.trim(),
                termsAgreed,
                privacyAgreed
        );
    }
}
