package com.yuhyeon.devwebide.auth.controller;

import com.yuhyeon.devwebide.auth.dto.AuthLogoutResponse;
import com.yuhyeon.devwebide.auth.dto.AuthTokenRefreshResponse;
import com.yuhyeon.devwebide.auth.dto.GoogleOAuthSignupRequest;
import com.yuhyeon.devwebide.auth.dto.OAuthLoginRequest;
import com.yuhyeon.devwebide.auth.dto.OAuthLoginResponse;
import com.yuhyeon.devwebide.auth.dto.OAuthLoginResult;
import com.yuhyeon.devwebide.auth.service.AuthService;
import com.yuhyeon.devwebide.auth.service.GoogleOAuthService;
import com.yuhyeon.devwebide.auth.service.OAuthLoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final String GOOGLE_OAUTH_STATE_COOKIE_NAME = "googleOAuthState";
    private static final String GOOGLE_OAUTH_SIGNUP_COOKIE_NAME = "googleOAuthSignup";
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String X_FORWARDED_PROTO = "X-Forwarded-Proto";
    private static final String CLOUDFRONT_FORWARDED_PROTO = "CloudFront-Forwarded-Proto";
    private static final Duration GOOGLE_OAUTH_COOKIE_MAX_AGE = Duration.ofMinutes(10);

    private final AuthService authService;
    private final OAuthLoginService oAuthLoginService;
    private final GoogleOAuthService googleOAuthService;

    @PostMapping("/oauth/login")
    public ResponseEntity<OAuthLoginResponse> oauthLogin(
            @Valid @RequestBody OAuthLoginRequest request,
            HttpServletRequest servletRequest
    ) {
        OAuthLoginResult result = oAuthLoginService.login(
                request,
                extractClientIp(servletRequest),
                servletRequest.getHeader(HttpHeaders.USER_AGENT)
        );

        ResponseCookie refreshTokenCookie = createRefreshTokenCookie(result, servletRequest);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(result.response());
    }

    @GetMapping("/oauth/google/authorize")
    public ResponseEntity<Void> startGoogleOAuth(
            @RequestParam String nickname,
            @RequestParam boolean termsAgreed,
            @RequestParam boolean privacyAgreed,
            HttpServletRequest servletRequest
    ) {
        validateGoogleSignup(nickname, termsAgreed, privacyAgreed);

        String state = googleOAuthService.createState();
        ResponseCookie stateCookie = createOAuthCookie(
                GOOGLE_OAUTH_STATE_COOKIE_NAME,
                state,
                GOOGLE_OAUTH_COOKIE_MAX_AGE,
                servletRequest
        );
        ResponseCookie signupCookie = createOAuthCookie(
                GOOGLE_OAUTH_SIGNUP_COOKIE_NAME,
                encodeGoogleSignupCookie(nickname, termsAgreed, privacyAgreed),
                GOOGLE_OAUTH_COOKIE_MAX_AGE,
                servletRequest
        );

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, googleOAuthService.buildAuthorizationUri(state))
                .header(HttpHeaders.SET_COOKIE, stateCookie.toString())
                .header(HttpHeaders.SET_COOKIE, signupCookie.toString())
                .build();
    }

    @GetMapping("/oauth/google/callback")
    public ResponseEntity<Void> handleGoogleOAuthCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            @CookieValue(name = GOOGLE_OAUTH_STATE_COOKIE_NAME, required = false) String savedState,
            @CookieValue(name = GOOGLE_OAUTH_SIGNUP_COOKIE_NAME, required = false) String signupCookie,
            HttpServletRequest servletRequest
    ) {
        ResponseCookie deleteStateCookie = deleteCookie(GOOGLE_OAUTH_STATE_COOKIE_NAME, servletRequest);
        ResponseCookie deleteSignupCookie = deleteCookie(GOOGLE_OAUTH_SIGNUP_COOKIE_NAME, servletRequest);

        if (error != null && !error.isBlank()) {
            return buildGoogleOAuthFailureRedirect(error, deleteStateCookie, deleteSignupCookie);
        }

        if (savedState == null || state == null || !savedState.equals(state) || signupCookie == null) {
            return buildGoogleOAuthFailureRedirect("invalid_state", deleteStateCookie, deleteSignupCookie);
        }

        try {
            OAuthLoginResult result = googleOAuthService.loginWithAuthorizationCode(
                    code,
                    decodeGoogleSignupCookie(signupCookie),
                    extractClientIp(servletRequest),
                    servletRequest.getHeader(HttpHeaders.USER_AGENT)
            );
            ResponseCookie refreshTokenCookie = createRefreshTokenCookie(result, servletRequest);

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, googleOAuthService.buildFrontendSuccessRedirectUri(result))
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, deleteStateCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, deleteSignupCookie.toString())
                    .build();
        } catch (RuntimeException exception) {
            return buildGoogleOAuthFailureRedirect(
                    "login_failed",
                    deleteStateCookie,
                    deleteSignupCookie
            );
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthLogoutResponse> logout(
            @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken
    ) {
        AuthLogoutResponse response = authService.logout(refreshToken);
        ResponseCookie deleteRefreshTokenCookie = deleteCookie(REFRESH_TOKEN_COOKIE_NAME, null);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteRefreshTokenCookie.toString())
                .body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokenRefreshResponse> refresh(
            @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken
    ) {
        AuthTokenRefreshResponse response = authService.refreshAccessToken(refreshToken);

        return ResponseEntity.ok(response);
    }

    private String extractClientIp(HttpServletRequest servletRequest) {
        String forwardedFor = servletRequest.getHeader(X_FORWARDED_FOR);
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return servletRequest.getRemoteAddr();
    }

    private ResponseEntity<Void> buildGoogleOAuthFailureRedirect(
            String reason,
            ResponseCookie deleteStateCookie,
            ResponseCookie deleteSignupCookie
    ) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, googleOAuthService.buildFrontendFailureRedirectUri(reason))
                .header(HttpHeaders.SET_COOKIE, deleteStateCookie.toString())
                .header(HttpHeaders.SET_COOKIE, deleteSignupCookie.toString())
                .build();
    }

    private ResponseCookie createRefreshTokenCookie(
            OAuthLoginResult result,
            HttpServletRequest servletRequest
    ) {
        return ResponseCookie
                .from(REFRESH_TOKEN_COOKIE_NAME, result.refreshToken())
                .httpOnly(true)
                .secure(isSecureRequest(servletRequest))
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofSeconds(result.refreshTokenExpiresInSeconds()))
                .build();
    }

    private ResponseCookie createOAuthCookie(
            String name,
            String value,
            Duration maxAge,
            HttpServletRequest servletRequest
    ) {
        return ResponseCookie
                .from(name, value)
                .httpOnly(true)
                .secure(isSecureRequest(servletRequest))
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();
    }

    private ResponseCookie deleteCookie(
            String name,
            HttpServletRequest servletRequest
    ) {
        return ResponseCookie
                .from(name, "")
                .httpOnly(true)
                .secure(isSecureRequest(servletRequest))
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
    }

    private boolean isSecureRequest(HttpServletRequest servletRequest) {
        if (servletRequest == null) {
            return false;
        }

        String forwardedProto = servletRequest.getHeader(X_FORWARDED_PROTO);
        String cloudFrontForwardedProto = servletRequest.getHeader(CLOUDFRONT_FORWARDED_PROTO);

        return servletRequest.isSecure()
                || "https".equalsIgnoreCase(forwardedProto)
                || "https".equalsIgnoreCase(cloudFrontForwardedProto);
    }

    private void validateGoogleSignup(
            String nickname,
            boolean termsAgreed,
            boolean privacyAgreed
    ) {
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("닉네임은 필수입니다.");
        }

        if (!termsAgreed || !privacyAgreed) {
            throw new IllegalArgumentException("약관과 개인정보 처리방침 동의가 필요합니다.");
        }
    }

    private String encodeGoogleSignupCookie(
            String nickname,
            boolean termsAgreed,
            boolean privacyAgreed
    ) {
        String value = URLEncoder.encode(nickname.trim(), StandardCharsets.UTF_8)
                + "|" + termsAgreed
                + "|" + privacyAgreed;

        return googleOAuthService.encodeCookieValue(value);
    }

    private GoogleOAuthSignupRequest decodeGoogleSignupCookie(String signupCookie) {
        String[] parts = googleOAuthService.decodeCookieValue(signupCookie).split("\\|", -1);

        if (parts.length != 3) {
            throw new IllegalArgumentException("Google OAuth 가입 정보를 확인할 수 없습니다.");
        }

        return new GoogleOAuthSignupRequest(
                URLDecoder.decode(parts[0], StandardCharsets.UTF_8),
                Boolean.parseBoolean(parts[1]),
                Boolean.parseBoolean(parts[2])
        );
    }
}
