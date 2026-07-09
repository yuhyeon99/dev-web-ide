package com.yuhyeon.devwebide.auth.security;

import com.yuhyeon.devwebide.auth.dto.AuthenticatedPrincipal;
import com.yuhyeon.devwebide.auth.service.TokenService;
import com.yuhyeon.devwebide.user.domain.*;
import com.yuhyeon.devwebide.user.repository.AuthSessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AccessTokenAuthenticationServiceTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private AuthSessionRepository authSessionRepository;

    @InjectMocks
    private AccessTokenAuthenticationService accessTokenAuthenticationService;

    @Test
    @DisplayName("회원 Access Token 인증에 성공한다")
    void authenticateUserToken() {
        AuthenticatedPrincipal principal = createUserPrincipal(100L, 1L);
        AuthSession authSession = createUserAuthSession(
                100L,
                1L,
                UserStatus.ACTIVE,
                LocalDateTime.now().plusDays(1)
        );

        given(tokenService.validateAccessToken("access-token"))
                .willReturn(principal);
        given(authSessionRepository.findById(100L))
                .willReturn(Optional.of(authSession));

        AuthenticatedPrincipal result =
                accessTokenAuthenticationService.authenticate("access-token");

        assertThat(result).isEqualTo(principal);
    }

    @Test
    @DisplayName("게스트 Access Token 인증에 성공한다")
    void authenticateGuestToken() {
        AuthenticatedPrincipal principal = createGuestPrincipal(200L, 10L);
        AuthSession authSession = createGuestAuthSession(
                200L,
                10L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1)
        );

        given(tokenService.validateAccessToken("access-token"))
                .willReturn(principal);
        given(authSessionRepository.findById(200L))
                .willReturn(Optional.of(authSession));

        AuthenticatedPrincipal result =
                accessTokenAuthenticationService.authenticate("access-token");

        assertThat(result).isEqualTo(principal);
    }

    @Test
    @DisplayName("AuthSession이 없으면 예외가 발생한다")
    void authenticateWithNotFoundAuthSession() {
        AuthenticatedPrincipal principal = createUserPrincipal(100L, 1L);

        given(tokenService.validateAccessToken("access-token"))
                .willReturn(principal);
        given(authSessionRepository.findById(100L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> accessTokenAuthenticationService.authenticate("access-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("인증 세션을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("폐기된 AuthSession이면 예외가 발생한다")
    void authenticateWithRevokedAuthSession() {
        AuthSession authSession = createUserAuthSession(
                100L,
                1L,
                UserStatus.ACTIVE,
                LocalDateTime.now().plusDays(1)
        );
        authSession.revoke(LocalDateTime.now());

        given(tokenService.validateAccessToken("access-token"))
                .willReturn(createUserPrincipal(100L, 1L));
        given(authSessionRepository.findById(100L))
                .willReturn(Optional.of(authSession));

        assertThatThrownBy(() -> accessTokenAuthenticationService.authenticate("access-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("폐기된 인증 세션입니다.");
    }

    @Test
    @DisplayName("만료된 AuthSession이면 예외가 발생한다")
    void authenticateWithExpiredAuthSession() {
        AuthSession authSession = createUserAuthSession(
                100L,
                1L,
                UserStatus.ACTIVE,
                LocalDateTime.now().minusDays(1)
        );

        given(tokenService.validateAccessToken("access-token"))
                .willReturn(createUserPrincipal(100L, 1L));
        given(authSessionRepository.findById(100L))
                .willReturn(Optional.of(authSession));

        assertThatThrownBy(() -> accessTokenAuthenticationService.authenticate("access-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("만료된 인증 세션입니다.");
    }

    @Test
    @DisplayName("회원 Principal인데 AuthSession이 회원 세션이 아니면 예외가 발생한다")
    void authenticateUserPrincipalWithGuestAuthSession() {
        AuthSession authSession = createGuestAuthSession(
                100L,
                10L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1)
        );

        given(tokenService.validateAccessToken("access-token"))
                .willReturn(createUserPrincipal(100L, 1L));
        given(authSessionRepository.findById(100L))
                .willReturn(Optional.of(authSession));

        assertThatThrownBy(() -> accessTokenAuthenticationService.authenticate("access-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 인증 세션이 아닙니다.");
    }

    @Test
    @DisplayName("회원 claim userId와 AuthSession user id가 다르면 예외가 발생한다")
    void authenticateUserPrincipalWithMismatchedUserId() {
        AuthSession authSession = createUserAuthSession(
                100L,
                1L,
                UserStatus.ACTIVE,
                LocalDateTime.now().plusDays(1)
        );

        given(tokenService.validateAccessToken("access-token"))
                .willReturn(createUserPrincipal(100L, 2L));
        given(authSessionRepository.findById(100L))
                .willReturn(Optional.of(authSession));

        assertThatThrownBy(() -> accessTokenAuthenticationService.authenticate("access-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Access Token의 사용자 정보가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("User가 INACTIVE이면 예외가 발생한다")
    void authenticateWithInactiveUser() {
        assertInactiveUserStatus(UserStatus.INACTIVE);
    }

    @Test
    @DisplayName("User가 DELETED이면 예외가 발생한다")
    void authenticateWithDeletedUser() {
        assertInactiveUserStatus(UserStatus.DELETED);
    }

    @Test
    @DisplayName("게스트 Principal인데 AuthSession이 게스트 세션이 아니면 예외가 발생한다")
    void authenticateGuestPrincipalWithUserAuthSession() {
        AuthSession authSession = createUserAuthSession(
                200L,
                1L,
                UserStatus.ACTIVE,
                LocalDateTime.now().plusDays(1)
        );

        given(tokenService.validateAccessToken("access-token"))
                .willReturn(createGuestPrincipal(200L, 10L));
        given(authSessionRepository.findById(200L))
                .willReturn(Optional.of(authSession));

        assertThatThrownBy(() -> accessTokenAuthenticationService.authenticate("access-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("게스트 인증 세션이 아닙니다.");
    }

    @Test
    @DisplayName("게스트 claim guestSessionId와 AuthSession guestSession id가 다르면 예외가 발생한다")
    void authenticateGuestPrincipalWithMismatchedGuestSessionId() {
        AuthSession authSession = createGuestAuthSession(
                200L,
                10L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1)
        );

        given(tokenService.validateAccessToken("access-token"))
                .willReturn(createGuestPrincipal(200L, 11L));
        given(authSessionRepository.findById(200L))
                .willReturn(Optional.of(authSession));

        assertThatThrownBy(() -> accessTokenAuthenticationService.authenticate("access-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Access Token의 게스트 세션 정보가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("GuestSession이 만료되면 예외가 발생한다")
    void authenticateWithExpiredGuestSession() {
        AuthSession authSession = createGuestAuthSession(
                200L,
                10L,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1)
        );

        given(tokenService.validateAccessToken("access-token"))
                .willReturn(createGuestPrincipal(200L, 10L));
        given(authSessionRepository.findById(200L))
                .willReturn(Optional.of(authSession));

        assertThatThrownBy(() -> accessTokenAuthenticationService.authenticate("access-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("만료된 게스트 세션입니다.");
    }

    private void assertInactiveUserStatus(UserStatus userStatus) {
        AuthSession authSession = createUserAuthSession(
                100L,
                1L,
                userStatus,
                LocalDateTime.now().plusDays(1)
        );

        given(tokenService.validateAccessToken("access-token"))
                .willReturn(createUserPrincipal(100L, 1L));
        given(authSessionRepository.findById(100L))
                .willReturn(Optional.of(authSession));

        assertThatThrownBy(() -> accessTokenAuthenticationService.authenticate("access-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("활성 상태의 사용자가 아닙니다.");
    }

    private AuthenticatedPrincipal createUserPrincipal(Long sessionId, Long userId) {
        return new AuthenticatedPrincipal(sessionId, "USER", userId, null, "USER");
    }

    private AuthenticatedPrincipal createGuestPrincipal(Long sessionId, Long guestSessionId) {
        return new AuthenticatedPrincipal(sessionId, "GUEST", null, guestSessionId, null);
    }

    private AuthSession createUserAuthSession(
            Long sessionId,
            Long userId,
            UserStatus status,
            LocalDateTime expiresAt
    ) {
        User user = User.builder()
                .email("user@test.com")
                .nickname("user")
                .role(UserRole.USER)
                .status(status)
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        AuthSession authSession = AuthSession.builder()
                .user(user)
                .refreshTokenHash("refresh-token-hash")
                .clientIp("127.0.0.1")
                .userAgent("Chrome")
                .expiresAt(expiresAt)
                .build();
        ReflectionTestUtils.setField(authSession, "id", sessionId);

        return authSession;
    }

    private AuthSession createGuestAuthSession(
            Long sessionId,
            Long guestSessionId,
            LocalDateTime guestSessionExpiresAt,
            LocalDateTime authSessionExpiresAt
    ) {
        GuestSession guestSession = GuestSession.builder()
                .guestToken("guest-token")
                .clientIp("127.0.0.1")
                .expiresAt(guestSessionExpiresAt)
                .build();
        ReflectionTestUtils.setField(guestSession, "id", guestSessionId);

        AuthSession authSession = AuthSession.builder()
                .guestSession(guestSession)
                .refreshTokenHash("refresh-token-hash")
                .clientIp("127.0.0.1")
                .userAgent("Chrome")
                .expiresAt(authSessionExpiresAt)
                .build();
        ReflectionTestUtils.setField(authSession, "id", sessionId);

        return authSession;
    }
}
