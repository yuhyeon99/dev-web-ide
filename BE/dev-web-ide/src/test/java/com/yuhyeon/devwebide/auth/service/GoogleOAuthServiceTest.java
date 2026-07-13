package com.yuhyeon.devwebide.auth.service;

import com.yuhyeon.devwebide.auth.dto.GoogleOAuthUserInfo;
import com.yuhyeon.devwebide.auth.dto.OAuthLoginResponse;
import com.yuhyeon.devwebide.auth.dto.OAuthLoginResult;
import com.yuhyeon.devwebide.user.domain.UserRole;
import com.yuhyeon.devwebide.user.domain.UserStatus;
import com.yuhyeon.devwebide.user.repository.OAuthAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class GoogleOAuthServiceTest {

    private final GoogleOAuthService googleOAuthService = new GoogleOAuthService(
            mock(OAuthLoginService.class),
            mock(OAuthAccountRepository.class)
    );

    @Test
    @DisplayName("FE 로그인 성공 리다이렉트 URL은 한글 닉네임을 UTF-8로 인코딩한다")
    void buildFrontendSuccessRedirectUriEncodesKoreanNickname() {
        setFrontendRedirectUri();
        OAuthLoginResult result = new OAuthLoginResult(
                new OAuthLoginResponse(
                        "access-token",
                        "Bearer",
                        1800L,
                        LocalDateTime.of(2026, 7, 13, 15, 0),
                        1L,
                        "user@test.com",
                        "김유현",
                        UserRole.USER,
                        UserStatus.ACTIVE,
                        false
                ),
                "refresh-token",
                1_209_600L
        );

        String redirectUri = googleOAuthService.buildFrontendSuccessRedirectUri(result);

        assertThat(redirectUri)
                .contains("nickname=%EA%B9%80%EC%9C%A0%ED%98%84")
                .doesNotContain("김유현");
    }

    @Test
    @DisplayName("FE 가입 필요 리다이렉트 URL은 Google 이름을 UTF-8로 인코딩한다")
    void buildFrontendSignupRedirectUriEncodesKoreanName() {
        setFrontendRedirectUri();

        String redirectUri = googleOAuthService.buildFrontendSignupRedirectUri(
                new GoogleOAuthUserInfo("google-user-id", "user@test.com", "김유현")
        );

        assertThat(redirectUri)
                .contains("name=%EA%B9%80%EC%9C%A0%ED%98%84")
                .doesNotContain("김유현");
    }

    @Test
    @DisplayName("Google 인증 URL은 scope 공백을 인코딩한다")
    void buildAuthorizationUriEncodesScope() {
        ReflectionTestUtils.setField(googleOAuthService, "clientId", "client-id");
        ReflectionTestUtils.setField(googleOAuthService, "clientSecret", "client-secret");
        ReflectionTestUtils.setField(
                googleOAuthService,
                "redirectUri",
                "https://api.example.com/api/auth/oauth/google/callback"
        );
        ReflectionTestUtils.setField(
                googleOAuthService,
                "authorizationUri",
                "https://accounts.google.com/o/oauth2/v2/auth"
        );

        String authorizationUri = googleOAuthService.buildAuthorizationUri("state-token");

        assertThat(authorizationUri)
                .contains("scope=openid%20email%20profile");
    }

    private void setFrontendRedirectUri() {
        ReflectionTestUtils.setField(
                googleOAuthService,
                "frontendRedirectUri",
                "https://d1qcnjd8lnakb.cloudfront.net"
        );
    }
}
