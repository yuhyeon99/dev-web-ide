package com.yuhyeon.devwebide.auth.controller;

import com.yuhyeon.devwebide.auth.dto.AuthLogoutResponse;
import com.yuhyeon.devwebide.auth.dto.AuthTokenRefreshResponse;
import com.yuhyeon.devwebide.auth.dto.OAuthLoginRequest;
import com.yuhyeon.devwebide.auth.dto.OAuthLoginResponse;
import com.yuhyeon.devwebide.auth.dto.OAuthLoginResult;
import com.yuhyeon.devwebide.auth.service.AuthService;
import com.yuhyeon.devwebide.auth.service.OAuthLoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    private final AuthService authService;
    private final OAuthLoginService oAuthLoginService;

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

        ResponseCookie refreshTokenCookie = ResponseCookie
                .from(REFRESH_TOKEN_COOKIE_NAME, result.refreshToken())
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofSeconds(result.refreshTokenExpiresInSeconds()))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(result.response());
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthLogoutResponse> logout(
            @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken
    ) {
        AuthLogoutResponse response = authService.logout(refreshToken);
        ResponseCookie deleteRefreshTokenCookie = ResponseCookie
                .from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();

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
}
