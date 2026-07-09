package com.yuhyeon.devwebide.auth.service;

import com.yuhyeon.devwebide.auth.dto.AuthLogoutResponse;
import com.yuhyeon.devwebide.auth.dto.AuthTokenRefreshResponse;
import com.yuhyeon.devwebide.user.domain.AuthSession;
import com.yuhyeon.devwebide.user.domain.UserStatus;
import com.yuhyeon.devwebide.user.repository.AuthSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final AuthSessionRepository authSessionRepository;
    private final TokenService tokenService;

    @Transactional
    public AuthLogoutResponse logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh Token이 필요합니다.");
        }

        String refreshTokenHash = tokenService.hashRefreshToken(refreshToken);
        AuthSession authSession = authSessionRepository.findByRefreshTokenHash(refreshTokenHash)
                .orElseThrow(() -> new IllegalArgumentException("인증 세션을 찾을 수 없습니다."));

        if (authSession.isRevoked()) {
            return AuthLogoutResponse.of(true, authSession.getRevokedAt());
        }

        LocalDateTime revokedAt = LocalDateTime.now();
        authSession.revoke(revokedAt);

        return AuthLogoutResponse.of(true, revokedAt);
    }

    public AuthTokenRefreshResponse refreshAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh Token이 필요합니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        String refreshTokenHash = tokenService.hashRefreshToken(refreshToken);
        AuthSession authSession = authSessionRepository.findByRefreshTokenHash(refreshTokenHash)
                .orElseThrow(() -> new IllegalArgumentException("인증 세션을 찾을 수 없습니다."));

        validateRefreshableSession(authSession, now);

        String accessToken = tokenService.createAccessToken(authSession, now);
        LocalDateTime accessTokenExpiresAt = tokenService.calculateAccessTokenExpiresAt(now);

        return AuthTokenRefreshResponse.of(
                accessToken,
                tokenService.getAccessTokenExpiresInSeconds(),
                accessTokenExpiresAt
        );
    }

    private void validateRefreshableSession(AuthSession authSession, LocalDateTime now) {
        if (authSession.isRevoked()) {
            throw new IllegalArgumentException("폐기된 인증 세션입니다.");
        }

        if (authSession.isExpired(now)) {
            throw new IllegalArgumentException("만료된 인증 세션입니다.");
        }

        if (authSession.isUserSession()
                && authSession.getUser().getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("활성 상태의 사용자가 아닙니다.");
        }

        if (authSession.isGuestSession()
                && authSession.getGuestSession().isExpired(now)) {
            throw new IllegalArgumentException("만료된 게스트 세션입니다.");
        }
    }
}
