package com.yuhyeon.devwebide.auth.service;

import com.yuhyeon.devwebide.auth.dto.AuthLogoutResponse;
import com.yuhyeon.devwebide.user.domain.AuthSession;
import com.yuhyeon.devwebide.user.domain.User;
import com.yuhyeon.devwebide.user.domain.UserRole;
import com.yuhyeon.devwebide.user.domain.UserStatus;
import com.yuhyeon.devwebide.user.repository.AuthSessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthSessionRepository authSessionRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Refresh Token 기반으로 로그아웃한다")
    void logout() {
        String refreshToken = "refresh-token";
        String refreshTokenHash = hash(refreshToken);
        AuthSession authSession = createAuthSession(LocalDateTime.now().plusDays(7));
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        given(authSessionRepository.findByRefreshTokenHash(refreshTokenHash))
                .willReturn(Optional.of(authSession));

        AuthLogoutResponse response = authService.logout(refreshToken);

        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertThat(response.loggedOut()).isTrue();
        assertThat(response.revokedAt()).isNotNull();
        assertThat(response.revokedAt()).isBetween(before, after);
        assertThat(authSession.isRevoked()).isTrue();
        assertThat(authSession.getRevokedAt()).isEqualTo(response.revokedAt());

        then(authSessionRepository).should()
                .findByRefreshTokenHash(refreshTokenHash);
    }

    @Test
    @DisplayName("Refresh Token이 null이면 예외가 발생한다")
    void logoutWithNullRefreshToken() {
        assertThatThrownBy(() -> authService.logout(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Refresh Token이 필요합니다.");
    }

    @Test
    @DisplayName("Refresh Token이 blank이면 예외가 발생한다")
    void logoutWithBlankRefreshToken() {
        assertThatThrownBy(() -> authService.logout(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Refresh Token이 필요합니다.");
    }

    @Test
    @DisplayName("존재하지 않는 Refresh Token이면 예외가 발생한다")
    void logoutWithNotFoundRefreshToken() {
        String refreshToken = "missing-refresh-token";
        String refreshTokenHash = hash(refreshToken);

        given(authSessionRepository.findByRefreshTokenHash(refreshTokenHash))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.logout(refreshToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("인증 세션을 찾을 수 없습니다.");

        then(authSessionRepository).should()
                .findByRefreshTokenHash(refreshTokenHash);
    }

    @Test
    @DisplayName("이미 revoke된 세션은 멱등 성공으로 처리한다")
    void logoutWithRevokedSession() {
        String refreshToken = "revoked-refresh-token";
        String refreshTokenHash = hash(refreshToken);
        LocalDateTime revokedAt = LocalDateTime.of(2026, 7, 8, 10, 0);
        AuthSession authSession = createAuthSession(LocalDateTime.now().plusDays(7));
        authSession.revoke(revokedAt);

        given(authSessionRepository.findByRefreshTokenHash(refreshTokenHash))
                .willReturn(Optional.of(authSession));

        AuthLogoutResponse response = authService.logout(refreshToken);

        assertThat(response.loggedOut()).isTrue();
        assertThat(response.revokedAt()).isEqualTo(revokedAt);
        assertThat(authSession.getRevokedAt()).isEqualTo(revokedAt);
    }

    @Test
    @DisplayName("만료된 세션도 revoke 처리 후 성공으로 처리한다")
    void logoutWithExpiredSession() {
        String refreshToken = "expired-refresh-token";
        String refreshTokenHash = hash(refreshToken);
        AuthSession authSession = createAuthSession(LocalDateTime.now().minusDays(1));

        given(authSessionRepository.findByRefreshTokenHash(refreshTokenHash))
                .willReturn(Optional.of(authSession));

        AuthLogoutResponse response = authService.logout(refreshToken);

        assertThat(response.loggedOut()).isTrue();
        assertThat(response.revokedAt()).isNotNull();
        assertThat(authSession.isRevoked()).isTrue();
        assertThat(authSession.getRevokedAt()).isEqualTo(response.revokedAt());
    }

    private AuthSession createAuthSession(LocalDateTime expiresAt) {
        User user = User.builder()
                .email("auth@test.com")
                .nickname("auth")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        return AuthSession.builder()
                .user(user)
                .refreshTokenHash("refresh-token-hash")
                .clientIp("127.0.0.1")
                .userAgent("Chrome")
                .expiresAt(expiresAt)
                .build();
    }

    private String hash(String value) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
