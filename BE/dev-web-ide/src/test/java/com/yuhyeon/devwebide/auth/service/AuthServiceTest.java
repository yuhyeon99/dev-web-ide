package com.yuhyeon.devwebide.auth.service;

import com.yuhyeon.devwebide.auth.dto.AuthLogoutResponse;
import com.yuhyeon.devwebide.auth.dto.AuthTokenRefreshResponse;
import com.yuhyeon.devwebide.user.domain.AuthSession;
import com.yuhyeon.devwebide.user.domain.GuestSession;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthSessionRepository authSessionRepository;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Refresh Token 기반으로 로그아웃한다")
    void logout() {
        String refreshToken = "refresh-token";
        String refreshTokenHash = "refresh-token-hash";
        AuthSession authSession = createAuthSession(LocalDateTime.now().plusDays(7));
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        given(tokenService.hashRefreshToken(refreshToken))
                .willReturn(refreshTokenHash);
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
        String refreshTokenHash = "missing-refresh-token-hash";

        given(tokenService.hashRefreshToken(refreshToken))
                .willReturn(refreshTokenHash);
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
        String refreshTokenHash = "revoked-refresh-token-hash";
        LocalDateTime revokedAt = LocalDateTime.of(2026, 7, 8, 10, 0);
        AuthSession authSession = createAuthSession(LocalDateTime.now().plusDays(7));
        authSession.revoke(revokedAt);

        given(tokenService.hashRefreshToken(refreshToken))
                .willReturn(refreshTokenHash);
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
        String refreshTokenHash = "expired-refresh-token-hash";
        AuthSession authSession = createAuthSession(LocalDateTime.now().minusDays(1));

        given(tokenService.hashRefreshToken(refreshToken))
                .willReturn(refreshTokenHash);
        given(authSessionRepository.findByRefreshTokenHash(refreshTokenHash))
                .willReturn(Optional.of(authSession));

        AuthLogoutResponse response = authService.logout(refreshToken);

        assertThat(response.loggedOut()).isTrue();
        assertThat(response.revokedAt()).isNotNull();
        assertThat(authSession.isRevoked()).isTrue();
        assertThat(authSession.getRevokedAt()).isEqualTo(response.revokedAt());
    }

    @Test
    @DisplayName("Refresh Token 기반으로 Access Token을 재발급한다")
    void refreshAccessToken() {
        String refreshToken = "refresh-token";
        String refreshTokenHash = "refresh-token-hash";
        AuthSession authSession = createAuthSession(LocalDateTime.now().plusDays(7));
        LocalDateTime beforeExpiresAt = LocalDateTime.now().plusMinutes(30).minusSeconds(1);

        given(tokenService.hashRefreshToken(refreshToken))
                .willReturn(refreshTokenHash);
        given(authSessionRepository.findByRefreshTokenHash(refreshTokenHash))
                .willReturn(Optional.of(authSession));
        given(tokenService.createAccessToken(any(AuthSession.class), any(LocalDateTime.class)))
                .willReturn("access-token");
        given(tokenService.calculateAccessTokenExpiresAt(any(LocalDateTime.class)))
                .willAnswer(invocation -> invocation.<LocalDateTime>getArgument(0).plusMinutes(30));
        given(tokenService.getAccessTokenExpiresInSeconds())
                .willReturn(1800L);

        AuthTokenRefreshResponse response = authService.refreshAccessToken(refreshToken);

        LocalDateTime afterExpiresAt = LocalDateTime.now().plusMinutes(30).plusSeconds(1);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(1800L);
        assertThat(response.accessTokenExpiresAt()).isBetween(beforeExpiresAt, afterExpiresAt);

        then(tokenService).should()
                .hashRefreshToken(refreshToken);
        then(authSessionRepository).should()
                .findByRefreshTokenHash(refreshTokenHash);
        then(tokenService).should()
                .createAccessToken(any(AuthSession.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Access Token 만료 시간은 30분 기준이다")
    void refreshAccessTokenExpiresAt() {
        String refreshToken = "refresh-token";
        String refreshTokenHash = "refresh-token-hash";
        AuthSession authSession = createAuthSession(LocalDateTime.now().plusDays(7));

        given(tokenService.hashRefreshToken(refreshToken))
                .willReturn(refreshTokenHash);
        given(authSessionRepository.findByRefreshTokenHash(refreshTokenHash))
                .willReturn(Optional.of(authSession));
        given(tokenService.createAccessToken(any(AuthSession.class), any(LocalDateTime.class)))
                .willReturn("access-token");
        given(tokenService.calculateAccessTokenExpiresAt(any(LocalDateTime.class)))
                .willAnswer(invocation -> invocation.<LocalDateTime>getArgument(0).plusMinutes(30));
        given(tokenService.getAccessTokenExpiresInSeconds())
                .willReturn(1800L);

        LocalDateTime beforeExpiresAt = LocalDateTime.now().plusMinutes(30).minusSeconds(1);
        AuthTokenRefreshResponse response = authService.refreshAccessToken(refreshToken);
        LocalDateTime afterExpiresAt = LocalDateTime.now().plusMinutes(30).plusSeconds(1);

        assertThat(response.accessTokenExpiresAt()).isBetween(beforeExpiresAt, afterExpiresAt);
        assertThat(response.expiresIn()).isEqualTo(1800L);
    }

    @Test
    @DisplayName("토큰 재발급 시 Refresh Token이 null이면 예외가 발생한다")
    void refreshAccessTokenWithNullRefreshToken() {
        assertThatThrownBy(() -> authService.refreshAccessToken(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Refresh Token이 필요합니다.");
    }

    @Test
    @DisplayName("토큰 재발급 시 Refresh Token이 blank이면 예외가 발생한다")
    void refreshAccessTokenWithBlankRefreshToken() {
        assertThatThrownBy(() -> authService.refreshAccessToken(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Refresh Token이 필요합니다.");
    }

    @Test
    @DisplayName("토큰 재발급 시 인증 세션이 없으면 예외가 발생한다")
    void refreshAccessTokenWithNotFoundSession() {
        String refreshToken = "missing-refresh-token";
        String refreshTokenHash = "missing-refresh-token-hash";

        given(tokenService.hashRefreshToken(refreshToken))
                .willReturn(refreshTokenHash);
        given(authSessionRepository.findByRefreshTokenHash(refreshTokenHash))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshAccessToken(refreshToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("인증 세션을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("토큰 재발급 시 폐기된 세션이면 예외가 발생한다")
    void refreshAccessTokenWithRevokedSession() {
        String refreshToken = "revoked-refresh-token";
        String refreshTokenHash = "revoked-refresh-token-hash";
        AuthSession authSession = createAuthSession(LocalDateTime.now().plusDays(7));
        authSession.revoke(LocalDateTime.now());

        given(tokenService.hashRefreshToken(refreshToken))
                .willReturn(refreshTokenHash);
        given(authSessionRepository.findByRefreshTokenHash(refreshTokenHash))
                .willReturn(Optional.of(authSession));

        assertThatThrownBy(() -> authService.refreshAccessToken(refreshToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("폐기된 인증 세션입니다.");

        then(tokenService).should(never())
                .createAccessToken(any(AuthSession.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("토큰 재발급 시 만료된 세션이면 예외가 발생한다")
    void refreshAccessTokenWithExpiredSession() {
        String refreshToken = "expired-refresh-token";
        String refreshTokenHash = "expired-refresh-token-hash";
        AuthSession authSession = createAuthSession(LocalDateTime.now().minusDays(1));

        given(tokenService.hashRefreshToken(refreshToken))
                .willReturn(refreshTokenHash);
        given(authSessionRepository.findByRefreshTokenHash(refreshTokenHash))
                .willReturn(Optional.of(authSession));

        assertThatThrownBy(() -> authService.refreshAccessToken(refreshToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("만료된 인증 세션입니다.");
    }

    @Test
    @DisplayName("토큰 재발급 시 사용자가 ACTIVE가 아니면 예외가 발생한다")
    void refreshAccessTokenWithInactiveUser() {
        String refreshToken = "inactive-user-refresh-token";
        String refreshTokenHash = "inactive-user-refresh-token-hash";
        AuthSession authSession = createAuthSession(
                LocalDateTime.now().plusDays(7),
                UserStatus.INACTIVE
        );

        given(tokenService.hashRefreshToken(refreshToken))
                .willReturn(refreshTokenHash);
        given(authSessionRepository.findByRefreshTokenHash(refreshTokenHash))
                .willReturn(Optional.of(authSession));

        assertThatThrownBy(() -> authService.refreshAccessToken(refreshToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("활성 상태의 사용자가 아닙니다.");
    }

    @Test
    @DisplayName("토큰 재발급 시 게스트 세션이 만료되면 예외가 발생한다")
    void refreshAccessTokenWithExpiredGuestSession() {
        String refreshToken = "expired-guest-refresh-token";
        String refreshTokenHash = "expired-guest-refresh-token-hash";
        AuthSession authSession = createGuestAuthSession(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(7)
        );

        given(tokenService.hashRefreshToken(refreshToken))
                .willReturn(refreshTokenHash);
        given(authSessionRepository.findByRefreshTokenHash(refreshTokenHash))
                .willReturn(Optional.of(authSession));

        assertThatThrownBy(() -> authService.refreshAccessToken(refreshToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("만료된 게스트 세션입니다.");
    }

    private AuthSession createAuthSession(LocalDateTime expiresAt) {
        return createAuthSession(expiresAt, UserStatus.ACTIVE);
    }

    private AuthSession createAuthSession(LocalDateTime expiresAt, UserStatus userStatus) {
        User user = User.builder()
                .email("auth@test.com")
                .nickname("auth")
                .role(UserRole.USER)
                .status(userStatus)
                .build();

        return AuthSession.builder()
                .user(user)
                .refreshTokenHash("refresh-token-hash")
                .clientIp("127.0.0.1")
                .userAgent("Chrome")
                .expiresAt(expiresAt)
                .build();
    }

    private AuthSession createGuestAuthSession(
            LocalDateTime guestSessionExpiresAt,
            LocalDateTime authSessionExpiresAt
    ) {
        GuestSession guestSession = GuestSession.builder()
                .guestToken("guest-token")
                .clientIp("127.0.0.1")
                .expiresAt(guestSessionExpiresAt)
                .build();

        return AuthSession.builder()
                .guestSession(guestSession)
                .refreshTokenHash("refresh-token-hash")
                .clientIp("127.0.0.1")
                .userAgent("Chrome")
                .expiresAt(authSessionExpiresAt)
                .build();
    }
}
