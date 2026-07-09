package com.yuhyeon.devwebide.auth.service;

import com.yuhyeon.devwebide.user.domain.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class TokenServiceTest {

    private static final String JWT_SECRET =
            "devwebide-local-development-secret-key-for-jwt-access-token";

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "jwtSecret", JWT_SECRET);
        ReflectionTestUtils.setField(tokenService, "accessTokenExpirationMinutes", 30L);
    }

    @Test
    @DisplayName("Refresh Token SHA-256 hash 결과를 반환한다")
    void hashRefreshToken() {
        String hash = tokenService.hashRefreshToken("refresh-token");

        assertThat(hash)
                .isEqualTo("0eb17643d4e9261163783a420859c92c7d212fa9624106a12b510afbec266120");
    }

    @Test
    @DisplayName("같은 Refresh Token은 같은 hash를 반환한다")
    void hashRefreshTokenWithSameRefreshToken() {
        String firstHash = tokenService.hashRefreshToken("same-refresh-token");
        String secondHash = tokenService.hashRefreshToken("same-refresh-token");

        assertThat(firstHash).isEqualTo(secondHash);
    }

    @Test
    @DisplayName("Access Token을 생성한다")
    void createAccessToken() {
        AuthSession authSession = createUserAuthSession();
        LocalDateTime issuedAt = LocalDateTime.of(2026, 7, 10, 10, 0);

        String accessToken = tokenService.createAccessToken(authSession, issuedAt);

        assertThat(accessToken).isNotBlank();
    }

    @Test
    @DisplayName("회원 세션 Access Token을 생성한다")
    void createUserSessionAccessToken() {
        AuthSession authSession = createUserAuthSession();
        LocalDateTime issuedAt = LocalDateTime.of(2026, 7, 10, 10, 0);

        String accessToken = tokenService.createAccessToken(authSession, issuedAt);
        Claims claims = parseClaims(accessToken);

        assertThat(claims.getSubject()).isEqualTo("user:1");
        assertThat(claims.get("sessionId", Long.class)).isEqualTo(100L);
        assertThat(claims.get("sessionType", String.class)).isEqualTo("USER");
        assertThat(claims.get("userId", Long.class)).isEqualTo(1L);
        assertThat(claims.get("role", String.class)).isEqualTo("USER");
        assertThat(claims.getIssuedAt()).isEqualTo(Date.from(
                issuedAt.atZone(java.time.ZoneId.systemDefault()).toInstant()
        ));
        assertThat(claims.getExpiration()).isEqualTo(Date.from(
                issuedAt.plusMinutes(30).atZone(java.time.ZoneId.systemDefault()).toInstant()
        ));
    }

    @Test
    @DisplayName("게스트 세션 Access Token을 생성한다")
    void createGuestSessionAccessToken() {
        AuthSession authSession = createGuestAuthSession();
        LocalDateTime issuedAt = LocalDateTime.of(2026, 7, 10, 10, 0);

        String accessToken = tokenService.createAccessToken(authSession, issuedAt);
        Claims claims = parseClaims(accessToken);

        assertThat(claims.getSubject()).isEqualTo("guest:10");
        assertThat(claims.get("sessionId", Long.class)).isEqualTo(200L);
        assertThat(claims.get("sessionType", String.class)).isEqualTo("GUEST");
        assertThat(claims.get("guestSessionId", Long.class)).isEqualTo(10L);
    }

    @Test
    @DisplayName("Access Token 만료 시간은 30분 기준이다")
    void calculateAccessTokenExpiresAt() {
        LocalDateTime issuedAt = LocalDateTime.of(2026, 7, 10, 10, 0);

        LocalDateTime expiresAt = tokenService.calculateAccessTokenExpiresAt(issuedAt);

        assertThat(expiresAt).isEqualTo(issuedAt.plusMinutes(30));
    }

    @Test
    @DisplayName("Access Token expiresIn은 1800초이다")
    void getAccessTokenExpiresInSeconds() {
        assertThat(tokenService.getAccessTokenExpiresInSeconds()).isEqualTo(1800L);
    }

    private Claims parseClaims(String accessToken) {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();
    }

    private AuthSession createUserAuthSession() {
        User user = User.builder()
                .email("user@test.com")
                .nickname("user")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        AuthSession authSession = AuthSession.builder()
                .user(user)
                .refreshTokenHash("refresh-token-hash")
                .clientIp("127.0.0.1")
                .userAgent("Chrome")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        ReflectionTestUtils.setField(authSession, "id", 100L);

        return authSession;
    }

    private AuthSession createGuestAuthSession() {
        GuestSession guestSession = GuestSession.builder()
                .guestToken("guest-token")
                .clientIp("127.0.0.1")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        ReflectionTestUtils.setField(guestSession, "id", 10L);

        AuthSession authSession = AuthSession.builder()
                .guestSession(guestSession)
                .refreshTokenHash("refresh-token-hash")
                .clientIp("127.0.0.1")
                .userAgent("Chrome")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        ReflectionTestUtils.setField(authSession, "id", 200L);

        return authSession;
    }
}
