package com.yuhyeon.devwebide.user.repository;

import com.yuhyeon.devwebide.user.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class AuthSessionRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GuestSessionRepository guestSessionRepository;

    @Autowired
    private AuthSessionRepository authSessionRepository;

    @Test
    @DisplayName("회원 인증 세션 저장")
    void saveUserAuthSession() {
        User user = User.builder()
                .email("auth-user@test.com")
                .nickname("회원세션테스터")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        AuthSession authSession = AuthSession.builder()
                .user(savedUser)
                .refreshTokenHash("user-refresh-token-hash")
                .clientIp("127.0.0.1")
                .userAgent("Chrome")
                .expiresAt(LocalDateTime.now().plusDays(14))
                .build();

        AuthSession savedAuthSession =
                authSessionRepository.save(authSession);

        assertThat(savedAuthSession.getId()).isNotNull();
        assertThat(savedAuthSession.getUser().getId())
                .isEqualTo(savedUser.getId());
        assertThat(savedAuthSession.getGuestSession()).isNull();
        assertThat(savedAuthSession.getRefreshTokenHash())
                .isEqualTo("user-refresh-token-hash");
        assertThat(savedAuthSession.isUserSession()).isTrue();
        assertThat(savedAuthSession.isGuestSession()).isFalse();
        assertThat(savedAuthSession.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("게스트 인증 세션 저장")
    void saveGuestAuthSession() {
        GuestSession guestSession = GuestSession.builder()
                .guestToken("guest-token-auth")
                .clientIp("127.0.0.1")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        GuestSession savedGuestSession =
                guestSessionRepository.save(guestSession);

        AuthSession authSession = AuthSession.builder()
                .guestSession(savedGuestSession)
                .refreshTokenHash("guest-refresh-token-hash")
                .clientIp("127.0.0.1")
                .userAgent("Chrome")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        AuthSession savedAuthSession =
                authSessionRepository.save(authSession);

        assertThat(savedAuthSession.getId()).isNotNull();
        assertThat(savedAuthSession.getUser()).isNull();
        assertThat(savedAuthSession.getGuestSession().getId())
                .isEqualTo(savedGuestSession.getId());
        assertThat(savedAuthSession.getRefreshTokenHash())
                .isEqualTo("guest-refresh-token-hash");
        assertThat(savedAuthSession.isUserSession()).isFalse();
        assertThat(savedAuthSession.isGuestSession()).isTrue();
    }

    @Test
    @DisplayName("Refresh Token 해시로 인증 세션 조회")
    void findByRefreshTokenHash() {
        User user = User.builder()
                .email("find-auth@test.com")
                .nickname("조회테스터")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        AuthSession authSession = AuthSession.builder()
                .user(savedUser)
                .refreshTokenHash("find-refresh-token-hash")
                .clientIp("127.0.0.1")
                .userAgent("Chrome")
                .expiresAt(LocalDateTime.now().plusDays(14))
                .build();

        authSessionRepository.save(authSession);

        Optional<AuthSession> result =
                authSessionRepository.findByRefreshTokenHash(
                        "find-refresh-token-hash"
                );

        assertThat(result).isPresent();
        assertThat(result.get().getRefreshTokenHash())
                .isEqualTo("find-refresh-token-hash");
        assertThat(result.get().getUser().getEmail())
                .isEqualTo("find-auth@test.com");
    }

    @Test
    @DisplayName("Refresh Token 해시 존재 여부 확인")
    void existsByRefreshTokenHash() {
        User user = User.builder()
                .email("exists-auth@test.com")
                .nickname("존재테스터")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        AuthSession authSession = AuthSession.builder()
                .user(savedUser)
                .refreshTokenHash("exists-refresh-token-hash")
                .clientIp("127.0.0.1")
                .userAgent("Chrome")
                .expiresAt(LocalDateTime.now().plusDays(14))
                .build();

        authSessionRepository.save(authSession);

        boolean exists =
                authSessionRepository.existsByRefreshTokenHash(
                        "exists-refresh-token-hash"
                );

        boolean notExists =
                authSessionRepository.existsByRefreshTokenHash(
                        "none-refresh-token-hash"
                );

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("유효한 Refresh Token 해시로 인증 세션 조회")
    void findActiveAuthSession() {
        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .email("active-auth@test.com")
                .nickname("활성세션테스터")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        AuthSession authSession = AuthSession.builder()
                .user(savedUser)
                .refreshTokenHash("active-refresh-token-hash")
                .clientIp("127.0.0.1")
                .userAgent("Chrome")
                .expiresAt(now.plusDays(14))
                .build();

        authSessionRepository.save(authSession);

        Optional<AuthSession> result =
                authSessionRepository
                        .findByRefreshTokenHashAndRevokedAtIsNullAndExpiresAtAfter(
                                "active-refresh-token-hash",
                                now
                        );

        assertThat(result).isPresent();
        assertThat(result.get().isActive(now)).isTrue();
    }

    @Test
    @DisplayName("폐기된 인증 세션은 유효한 세션 조회에서 제외")
    void revokedAuthSessionIsNotActive() {
        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .email("revoked-auth@test.com")
                .nickname("폐기세션테스터")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        AuthSession authSession = AuthSession.builder()
                .user(savedUser)
                .refreshTokenHash("revoked-refresh-token-hash")
                .clientIp("127.0.0.1")
                .userAgent("Chrome")
                .expiresAt(now.plusDays(14))
                .build();

        authSession.revoke(now);

        authSessionRepository.save(authSession);

        Optional<AuthSession> result =
                authSessionRepository
                        .findByRefreshTokenHashAndRevokedAtIsNullAndExpiresAtAfter(
                                "revoked-refresh-token-hash",
                                now
                        );

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("만료된 인증 세션은 유효한 세션 조회에서 제외")
    void expiredAuthSessionIsNotActive() {
        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .email("expired-auth@test.com")
                .nickname("만료세션테스터")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        AuthSession authSession = AuthSession.builder()
                .user(savedUser)
                .refreshTokenHash("expired-refresh-token-hash")
                .clientIp("127.0.0.1")
                .userAgent("Chrome")
                .expiresAt(now.minusDays(1))
                .build();

        authSessionRepository.save(authSession);

        Optional<AuthSession> result =
                authSessionRepository
                        .findByRefreshTokenHashAndRevokedAtIsNullAndExpiresAtAfter(
                                "expired-refresh-token-hash",
                                now
                        );

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("회원 세션과 게스트 세션이 모두 없으면 생성 실패")
    void createAuthSessionWithoutOwnerFail() {
        assertThatThrownBy(() -> AuthSession.builder()
                .refreshTokenHash("invalid-refresh-token-hash")
                .clientIp("127.0.0.1")
                .userAgent("Chrome")
                .expiresAt(LocalDateTime.now().plusDays(14))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 세션 또는 게스트 세션 중 하나만 지정해야 합니다.");
    }

    @Test
    @DisplayName("회원 세션과 게스트 세션이 모두 있으면 생성 실패")
    void createAuthSessionWithUserAndGuestSessionFail() {
        User user = User.builder()
                .email("invalid-owner@test.com")
                .nickname("잘못된세션테스터")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        GuestSession guestSession = GuestSession.builder()
                .guestToken("invalid-owner-guest-token")
                .clientIp("127.0.0.1")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        GuestSession savedGuestSession =
                guestSessionRepository.save(guestSession);

        assertThatThrownBy(() -> AuthSession.builder()
                .user(savedUser)
                .guestSession(savedGuestSession)
                .refreshTokenHash("invalid-owner-refresh-token-hash")
                .clientIp("127.0.0.1")
                .userAgent("Chrome")
                .expiresAt(LocalDateTime.now().plusDays(14))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 세션 또는 게스트 세션 중 하나만 지정해야 합니다.");
    }
}