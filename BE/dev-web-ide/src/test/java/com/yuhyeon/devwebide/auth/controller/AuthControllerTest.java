package com.yuhyeon.devwebide.auth.controller;

import com.yuhyeon.devwebide.auth.dto.AuthLogoutResponse;
import com.yuhyeon.devwebide.auth.dto.AuthTokenRefreshResponse;
import com.yuhyeon.devwebide.auth.dto.GoogleOAuthUserInfo;
import com.yuhyeon.devwebide.auth.dto.OAuthLoginResponse;
import com.yuhyeon.devwebide.auth.dto.OAuthLoginResult;
import com.yuhyeon.devwebide.auth.security.AccessTokenAuthenticationService;
import com.yuhyeon.devwebide.auth.service.AuthService;
import com.yuhyeon.devwebide.auth.service.GoogleOAuthService;
import com.yuhyeon.devwebide.auth.service.OAuthLoginService;
import com.yuhyeon.devwebide.user.domain.UserRole;
import com.yuhyeon.devwebide.user.domain.UserStatus;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private OAuthLoginService oAuthLoginService;

    @MockitoBean
    private GoogleOAuthService googleOAuthService;

    @MockitoBean
    private AccessTokenAuthenticationService accessTokenAuthenticationService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("Mock OAuth 로그인 API 요청에 성공한다")
    void oauthLogin() throws Exception {
        LocalDateTime accessTokenExpiresAt = LocalDateTime.of(2026, 7, 9, 10, 30);
        OAuthLoginResponse response = new OAuthLoginResponse(
                "access-token",
                "Bearer",
                1800L,
                accessTokenExpiresAt,
                1L,
                "user@test.com",
                "user",
                UserRole.USER,
                UserStatus.ACTIVE,
                true
        );
        OAuthLoginResult result = new OAuthLoginResult(
                response,
                "refresh-token",
                1_209_600L
        );

        given(oAuthLoginService.login(any(), eq("127.0.0.1"), eq("Chrome")))
                .willReturn(result);

        mockMvc.perform(post("/api/auth/oauth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.USER_AGENT, "Chrome")
                        .content("""
                                {
                                  "provider": "GITHUB",
                                  "providerUserId": "github-user-id",
                                  "providerEmail": "user@test.com",
                                  "nickname": "user",
                                  "termsAgreed": true,
                                  "privacyAgreed": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(1800L))
                .andExpect(jsonPath("$.accessTokenExpiresAt").exists())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.nickname").value("user"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.newUser").value(true))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString("refreshToken=refresh-token")
                ))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString("HttpOnly")
                ))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString("Path=/")
                ))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString("Max-Age=1209600")
                ))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString("Secure")
                ))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString("SameSite=None")
                ));

        ArgumentCaptor<com.yuhyeon.devwebide.auth.dto.OAuthLoginRequest> requestCaptor =
                ArgumentCaptor.forClass(com.yuhyeon.devwebide.auth.dto.OAuthLoginRequest.class);
        then(oAuthLoginService).should()
                .login(requestCaptor.capture(), eq("127.0.0.1"), eq("Chrome"));
        assertThat(requestCaptor.getValue().provider().name()).isEqualTo("GITHUB");
        assertThat(requestCaptor.getValue().providerUserId()).isEqualTo("github-user-id");
        assertThat(requestCaptor.getValue().providerEmail()).isEqualTo("user@test.com");
        assertThat(requestCaptor.getValue().nickname()).isEqualTo("user");
        assertThat(requestCaptor.getValue().termsAgreed()).isTrue();
        assertThat(requestCaptor.getValue().privacyAgreed()).isTrue();
    }

    @Test
    @DisplayName("X-Forwarded-For가 있으면 첫 번째 IP를 Service로 전달한다")
    void oauthLoginWithXForwardedFor() throws Exception {
        OAuthLoginResponse response = new OAuthLoginResponse(
                "access-token",
                "Bearer",
                1800L,
                LocalDateTime.of(2026, 7, 9, 10, 30),
                1L,
                "user@test.com",
                "user",
                UserRole.USER,
                UserStatus.ACTIVE,
                false
        );
        OAuthLoginResult result = new OAuthLoginResult(
                response,
                "refresh-token",
                1_209_600L
        );

        given(oAuthLoginService.login(any(), eq("203.0.113.10"), eq("Chrome")))
                .willReturn(result);

        mockMvc.perform(post("/api/auth/oauth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.USER_AGENT, "Chrome")
                        .header("X-Forwarded-For", "203.0.113.10, 10.0.0.1")
                        .content("""
                                {
                                  "provider": "GITHUB",
                                  "providerUserId": "github-user-id",
                                  "providerEmail": "user@test.com",
                                  "nickname": "user",
                                  "termsAgreed": true,
                                  "privacyAgreed": true
                                }
                                """))
                .andExpect(status().isOk());

        then(oAuthLoginService).should()
                .login(any(), eq("203.0.113.10"), eq("Chrome"));
    }

    @Test
    @DisplayName("Google OAuth 인증 시작 시 state 쿠키를 저장하고 Google로 리다이렉트한다")
    void startGoogleOAuth() throws Exception {
        given(googleOAuthService.createState())
                .willReturn("state-token");
        given(googleOAuthService.buildAuthorizationUri("state-token"))
                .willReturn("https://accounts.google.com/o/oauth2/v2/auth?state=state-token");

        mockMvc.perform(get("/api/auth/oauth/google/authorize")
                        .header("X-Forwarded-Proto", "https"))
                .andExpect(status().isFound())
                .andExpect(header().string(
                        HttpHeaders.LOCATION,
                        "https://accounts.google.com/o/oauth2/v2/auth?state=state-token"
                ))
                .andExpect(result -> {
                    assertThat(result.getResponse().getHeaders(HttpHeaders.SET_COOKIE))
                            .anyMatch(cookie -> cookie.contains("googleOAuthState=state-token"))
                            .noneMatch(cookie -> cookie.contains("googleOAuthPendingUser="));
                });
    }

    @Test
    @DisplayName("Google OAuth 콜백 성공 시 로그인 결과를 FE로 리다이렉트하고 refresh token을 쿠키에 저장한다")
    void handleGoogleOAuthCallback() throws Exception {
        LocalDateTime accessTokenExpiresAt = LocalDateTime.of(2026, 7, 9, 10, 30);
        OAuthLoginResponse response = new OAuthLoginResponse(
                "access-token",
                "Bearer",
                1800L,
                accessTokenExpiresAt,
                1L,
                "user@test.com",
                "user",
                UserRole.USER,
                UserStatus.ACTIVE,
                true
        );
        OAuthLoginResult result = new OAuthLoginResult(
                response,
                "refresh-token",
                1_209_600L
        );
        GoogleOAuthUserInfo userInfo = new GoogleOAuthUserInfo(
                "google-user-id",
                "user@test.com",
                "user"
        );

        given(googleOAuthService.fetchUserInfoWithAuthorizationCode("google-code"))
                .willReturn(userInfo);
        given(googleOAuthService.isRegisteredGoogleUser(userInfo))
                .willReturn(true);
        given(googleOAuthService.loginExistingGoogleUser(
                eq(userInfo),
                eq("127.0.0.1"),
                eq("Chrome")
        )).willReturn(result);
        given(googleOAuthService.buildFrontendSuccessRedirectUri(result))
                .willReturn("https://d1qcnjd8lnakb.cloudfront.net?oauth=success");

        mockMvc.perform(get("/api/auth/oauth/google/callback")
                        .param("code", "google-code")
                        .param("state", "state-token")
                        .cookie(new Cookie("googleOAuthState", "state-token"))
                        .header(HttpHeaders.USER_AGENT, "Chrome")
                        .header("X-Forwarded-Proto", "https"))
                .andExpect(status().isFound())
                .andExpect(header().string(
                        HttpHeaders.LOCATION,
                        "https://d1qcnjd8lnakb.cloudfront.net?oauth=success"
                ))
                .andExpect(mvcResult -> {
                    assertThat(mvcResult.getResponse().getHeaders(HttpHeaders.SET_COOKIE))
                            .anyMatch(cookie -> cookie.contains("refreshToken=refresh-token")
                                    && cookie.contains("Secure")
                                    && cookie.contains("SameSite=None"))
                            .anyMatch(cookie -> cookie.contains("googleOAuthState="))
                            .anyMatch(cookie -> cookie.contains("googleOAuthPendingUser="));
                });
    }

    @Test
    @DisplayName("Google OAuth 신규 사용자 콜백 시 가입 완료가 필요한 상태로 FE에 리다이렉트한다")
    void handleGoogleOAuthCallbackWithNewUser() throws Exception {
        GoogleOAuthUserInfo userInfo = new GoogleOAuthUserInfo(
                "google-user-id",
                "user@test.com",
                "user"
        );

        given(googleOAuthService.fetchUserInfoWithAuthorizationCode("google-code"))
                .willReturn(userInfo);
        given(googleOAuthService.isRegisteredGoogleUser(userInfo))
                .willReturn(false);
        given(googleOAuthService.encodeCookieValue("google-user-id|user%40test.com|user"))
                .willReturn("encoded-pending-user");
        given(googleOAuthService.buildFrontendSignupRedirectUri(userInfo))
                .willReturn("https://d1qcnjd8lnakb.cloudfront.net?oauth=signup_required");

        mockMvc.perform(get("/api/auth/oauth/google/callback")
                        .param("code", "google-code")
                        .param("state", "state-token")
                        .cookie(new Cookie("googleOAuthState", "state-token"))
                        .header(HttpHeaders.USER_AGENT, "Chrome")
                        .header("X-Forwarded-Proto", "https"))
                .andExpect(status().isFound())
                .andExpect(header().string(
                        HttpHeaders.LOCATION,
                        "https://d1qcnjd8lnakb.cloudfront.net?oauth=signup_required"
                ))
                .andExpect(mvcResult -> {
                    assertThat(mvcResult.getResponse().getHeaders(HttpHeaders.SET_COOKIE))
                            .anyMatch(cookie -> cookie.contains("googleOAuthPendingUser=encoded-pending-user")
                                    && cookie.contains("Secure")
                                    && cookie.contains("SameSite=None"))
                            .anyMatch(cookie -> cookie.contains("googleOAuthState="));
                });
    }

    @Test
    @DisplayName("로그아웃 API 요청에 성공한다")
    void logout() throws Exception {
        String refreshToken = "refresh-token";
        LocalDateTime revokedAt = LocalDateTime.of(2026, 7, 8, 10, 0);
        AuthLogoutResponse response = AuthLogoutResponse.of(true, revokedAt);

        given(authService.logout(refreshToken))
                .willReturn(response);

        mockMvc.perform(post("/api/auth/logout")
                        .cookie(new Cookie("refreshToken", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loggedOut").value(true))
                .andExpect(jsonPath("$.revokedAt").exists())
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString("refreshToken=")
                ))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString("Path=/")
                ))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString("Max-Age=0")
                ))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString("HttpOnly")
                ));

        then(authService).should()
                .logout(refreshToken);
    }

    @Test
    @DisplayName("Refresh Token Cookie가 없으면 Service 예외 흐름으로 연결된다")
    void logoutWithoutRefreshTokenCookie() {
        given(authService.logout(null))
                .willThrow(new IllegalArgumentException("Refresh Token이 필요합니다."));

        assertThatThrownBy(() -> mockMvc.perform(post("/api/auth/logout")))
                .hasCauseInstanceOf(IllegalArgumentException.class);

        then(authService).should()
                .logout(null);
    }

    @Test
    @DisplayName("토큰 재발급 API 요청에 성공한다")
    void refresh() throws Exception {
        String refreshToken = "refresh-token";
        LocalDateTime accessTokenExpiresAt = LocalDateTime.of(2026, 7, 9, 10, 30);
        AuthTokenRefreshResponse response = AuthTokenRefreshResponse.of(
                "access-token",
                1800L,
                accessTokenExpiresAt
        );

        given(authService.refreshAccessToken(refreshToken))
                .willReturn(response);

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refreshToken", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(1800L))
                .andExpect(jsonPath("$.accessTokenExpiresAt").exists());

        then(authService).should()
                .refreshAccessToken(refreshToken);
    }

    @Test
    @DisplayName("토큰 재발급 시 Refresh Token Cookie가 없으면 Service 예외 흐름으로 연결된다")
    void refreshWithoutRefreshTokenCookie() {
        given(authService.refreshAccessToken(null))
                .willThrow(new IllegalArgumentException("Refresh Token이 필요합니다."));

        assertThatThrownBy(() -> mockMvc.perform(post("/api/auth/refresh")))
                .hasCauseInstanceOf(IllegalArgumentException.class);

        then(authService).should()
                .refreshAccessToken(null);
    }
}
