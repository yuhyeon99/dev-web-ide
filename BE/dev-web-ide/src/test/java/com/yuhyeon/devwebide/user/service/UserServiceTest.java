package com.yuhyeon.devwebide.user.service;

import com.yuhyeon.devwebide.auth.dto.AuthenticatedPrincipal;
import com.yuhyeon.devwebide.auth.service.TokenService;
import com.yuhyeon.devwebide.user.domain.*;
import com.yuhyeon.devwebide.user.dto.UserMeResponse;
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
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private AuthSessionRepository authSessionRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원 Access Token으로 현재 사용자를 조회한다")
    void getCurrentUser() {
        String accessToken = "access-token";
        String authorizationHeader = "Bearer " + accessToken;
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
                100L,
                "USER",
                1L,
                null,
                "USER"
        );
        AuthSession authSession = createUserAuthSession(
                100L,
                1L,
                UserStatus.ACTIVE,
                LocalDateTime.now().plusDays(7)
        );

        given(tokenService.validateAccessToken(accessToken))
                .willReturn(principal);
        given(authSessionRepository.findById(100L))
                .willReturn(Optional.of(authSession));

        UserMeResponse response = userService.getCurrentUser(authorizationHeader);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("user@test.com");
        assertThat(response.nickname()).isEqualTo("user");
        assertThat(response.role()).isEqualTo(UserRole.USER);
        assertThat(response.status()).isEqualTo(UserStatus.ACTIVE);

        then(tokenService).should()
                .validateAccessToken(accessToken);
        then(authSessionRepository).should()
                .findById(100L);
    }

    @Test
    @DisplayName("Authorization header가 null이면 예외가 발생한다")
    void getCurrentUserWithNullAuthorizationHeader() {
        assertThatThrownBy(() -> userService.getCurrentUser(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Authorization header가 필요합니다.");
    }

    @Test
    @DisplayName("Authorization header가 blank이면 예외가 발생한다")
    void getCurrentUserWithBlankAuthorizationHeader() {
        assertThatThrownBy(() -> userService.getCurrentUser(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Authorization header가 필요합니다.");
    }

    @Test
    @DisplayName("Bearer prefix가 없으면 예외가 발생한다")
    void getCurrentUserWithoutBearerPrefix() {
        assertThatThrownBy(() -> userService.getCurrentUser("access-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bearer Access Token이 필요합니다.");
    }

    @Test
    @DisplayName("게스트 Access Token이면 예외가 발생한다")
    void getCurrentUserWithGuestToken() {
        String accessToken = "guest-access-token";
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
                200L,
                "GUEST",
                null,
                10L,
                null
        );

        given(tokenService.validateAccessToken(accessToken))
                .willReturn(principal);

        assertThatThrownBy(() -> userService.getCurrentUser("Bearer " + accessToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 Access Token이 필요합니다.");
    }

    @Test
    @DisplayName("AuthSession이 없으면 예외가 발생한다")
    void getCurrentUserWithNotFoundAuthSession() {
        String accessToken = "access-token";
        AuthenticatedPrincipal principal = createUserPrincipal(100L, 1L);

        given(tokenService.validateAccessToken(accessToken))
                .willReturn(principal);
        given(authSessionRepository.findById(100L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCurrentUser("Bearer " + accessToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("인증 세션을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("폐기된 AuthSession이면 예외가 발생한다")
    void getCurrentUserWithRevokedAuthSession() {
        String accessToken = "access-token";
        AuthSession authSession = createUserAuthSession(
                100L,
                1L,
                UserStatus.ACTIVE,
                LocalDateTime.now().plusDays(7)
        );
        authSession.revoke(LocalDateTime.now());

        given(tokenService.validateAccessToken(accessToken))
                .willReturn(createUserPrincipal(100L, 1L));
        given(authSessionRepository.findById(100L))
                .willReturn(Optional.of(authSession));

        assertThatThrownBy(() -> userService.getCurrentUser("Bearer " + accessToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("폐기된 인증 세션입니다.");
    }

    @Test
    @DisplayName("만료된 AuthSession이면 예외가 발생한다")
    void getCurrentUserWithExpiredAuthSession() {
        String accessToken = "access-token";
        AuthSession authSession = createUserAuthSession(
                100L,
                1L,
                UserStatus.ACTIVE,
                LocalDateTime.now().minusDays(1)
        );

        given(tokenService.validateAccessToken(accessToken))
                .willReturn(createUserPrincipal(100L, 1L));
        given(authSessionRepository.findById(100L))
                .willReturn(Optional.of(authSession));

        assertThatThrownBy(() -> userService.getCurrentUser("Bearer " + accessToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("만료된 인증 세션입니다.");
    }

    @Test
    @DisplayName("AuthSession이 회원 세션이 아니면 예외가 발생한다")
    void getCurrentUserWithGuestAuthSession() {
        String accessToken = "access-token";
        AuthSession authSession = createGuestAuthSession(100L);

        given(tokenService.validateAccessToken(accessToken))
                .willReturn(createUserPrincipal(100L, 1L));
        given(authSessionRepository.findById(100L))
                .willReturn(Optional.of(authSession));

        assertThatThrownBy(() -> userService.getCurrentUser("Bearer " + accessToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 인증 세션이 아닙니다.");
    }

    @Test
    @DisplayName("claim userId와 AuthSession user id가 다르면 예외가 발생한다")
    void getCurrentUserWithMismatchedUserId() {
        String accessToken = "access-token";
        AuthSession authSession = createUserAuthSession(
                100L,
                1L,
                UserStatus.ACTIVE,
                LocalDateTime.now().plusDays(7)
        );

        given(tokenService.validateAccessToken(accessToken))
                .willReturn(createUserPrincipal(100L, 2L));
        given(authSessionRepository.findById(100L))
                .willReturn(Optional.of(authSession));

        assertThatThrownBy(() -> userService.getCurrentUser("Bearer " + accessToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Access Token의 사용자 정보가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("사용자가 INACTIVE 상태이면 예외가 발생한다")
    void getCurrentUserWithInactiveUser() {
        assertInactiveUserStatus(UserStatus.INACTIVE);
    }

    @Test
    @DisplayName("사용자가 DELETED 상태이면 예외가 발생한다")
    void getCurrentUserWithDeletedUser() {
        assertInactiveUserStatus(UserStatus.DELETED);
    }

    private void assertInactiveUserStatus(UserStatus userStatus) {
        String accessToken = "access-token";
        AuthSession authSession = createUserAuthSession(
                100L,
                1L,
                userStatus,
                LocalDateTime.now().plusDays(7)
        );

        given(tokenService.validateAccessToken(accessToken))
                .willReturn(createUserPrincipal(100L, 1L));
        given(authSessionRepository.findById(100L))
                .willReturn(Optional.of(authSession));

        assertThatThrownBy(() -> userService.getCurrentUser("Bearer " + accessToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("활성 상태의 사용자가 아닙니다.");
    }

    private AuthenticatedPrincipal createUserPrincipal(Long sessionId, Long userId) {
        return new AuthenticatedPrincipal(
                sessionId,
                "USER",
                userId,
                null,
                "USER"
        );
    }

    private AuthSession createUserAuthSession(
            Long sessionId,
            Long userId,
            UserStatus userStatus,
            LocalDateTime expiresAt
    ) {
        User user = User.builder()
                .email("user@test.com")
                .nickname("user")
                .role(UserRole.USER)
                .status(userStatus)
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

    private AuthSession createGuestAuthSession(Long sessionId) {
        GuestSession guestSession = GuestSession.builder()
                .guestToken("guest-token")
                .clientIp("127.0.0.1")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        AuthSession authSession = AuthSession.builder()
                .guestSession(guestSession)
                .refreshTokenHash("refresh-token-hash")
                .clientIp("127.0.0.1")
                .userAgent("Chrome")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        ReflectionTestUtils.setField(authSession, "id", sessionId);

        return authSession;
    }
}
