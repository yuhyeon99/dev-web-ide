package com.yuhyeon.devwebide.auth.service;

import com.yuhyeon.devwebide.auth.dto.AuthenticatedPrincipal;
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
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    @DisplayName("Refresh Token을 생성한다")
    void createRefreshToken() {
        String refreshToken = tokenService.createRefreshToken();

        assertThat(refreshToken).isNotBlank();
    }

    @Test
    @DisplayName("Refresh Token은 매번 다른 값을 반환한다")
    void createRefreshTokenReturnsDifferentValue() {
        String firstRefreshToken = tokenService.createRefreshToken();
        String secondRefreshToken = tokenService.createRefreshToken();

        assertThat(firstRefreshToken).isNotEqualTo(secondRefreshToken);
    }

    @Test
    @DisplayName("Refresh Token 만료 시간은 14일 기준이다")
    void calculateRefreshTokenExpiresAt() {
        LocalDateTime issuedAt = LocalDateTime.of(2026, 7, 10, 10, 0);

        LocalDateTime expiresAt = tokenService.calculateRefreshTokenExpiresAt(issuedAt);

        assertThat(expiresAt).isEqualTo(issuedAt.plusDays(14));
    }

    @Test
    @DisplayName("Refresh Token expiresIn은 14일을 초 단위로 반환한다")
    void getRefreshTokenExpiresInSeconds() {
        assertThat(tokenService.getRefreshTokenExpiresInSeconds()).isEqualTo(1_209_600L);
    }

    @Test
    @DisplayName("유효한 회원 Access Token을 검증한다")
    void validateUserAccessToken() {
        String accessToken = tokenService.createAccessToken(
                createUserAuthSession(),
                LocalDateTime.of(2026, 7, 10, 10, 0)
        );

        AuthenticatedPrincipal principal = tokenService.validateAccessToken(accessToken);

        assertThat(principal.sessionId()).isEqualTo(100L);
        assertThat(principal.sessionType()).isEqualTo("USER");
        assertThat(principal.userId()).isEqualTo(1L);
        assertThat(principal.guestSessionId()).isNull();
        assertThat(principal.role()).isEqualTo("USER");
    }

    @Test
    @DisplayName("유효한 게스트 Access Token을 검증한다")
    void validateGuestAccessToken() {
        String accessToken = tokenService.createAccessToken(
                createGuestAuthSession(),
                LocalDateTime.of(2026, 7, 10, 10, 0)
        );

        AuthenticatedPrincipal principal = tokenService.validateAccessToken(accessToken);

        assertThat(principal.sessionId()).isEqualTo(200L);
        assertThat(principal.sessionType()).isEqualTo("GUEST");
        assertThat(principal.userId()).isNull();
        assertThat(principal.guestSessionId()).isEqualTo(10L);
        assertThat(principal.role()).isNull();
    }

    @Test
    @DisplayName("signature가 유효하지 않은 Access Token이면 예외가 발생한다")
    void validateAccessTokenWithInvalidSignature() {
        String accessToken = createToken("different-secret-key-for-invalid-signature-test")
                .subject("user:1")
                .claim("sessionId", 100L)
                .claim("sessionType", "USER")
                .claim("userId", 1L)
                .claim("role", "USER")
                .issuedAt(toDate(LocalDateTime.of(2026, 7, 10, 10, 0)))
                .expiration(toDate(LocalDateTime.of(2026, 7, 10, 10, 30)))
                .compact();

        assertThatThrownBy(() -> tokenService.validateAccessToken(accessToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 Access Token입니다.");
    }

    @Test
    @DisplayName("만료된 Access Token이면 예외가 발생한다")
    void validateAccessTokenWithExpiredToken() {
        String accessToken = createToken(JWT_SECRET)
                .subject("user:1")
                .claim("sessionId", 100L)
                .claim("sessionType", "USER")
                .claim("userId", 1L)
                .claim("role", "USER")
                .issuedAt(toDate(LocalDateTime.of(2026, 7, 8, 10, 0)))
                .expiration(toDate(LocalDateTime.of(2026, 7, 8, 10, 30)))
                .compact();

        assertThatThrownBy(() -> tokenService.validateAccessToken(accessToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 Access Token입니다.");
    }

    @Test
    @DisplayName("sessionType claim이 없으면 예외가 발생한다")
    void validateAccessTokenWithoutSessionType() {
        String accessToken = createToken(JWT_SECRET)
                .subject("user:1")
                .claim("sessionId", 100L)
                .claim("userId", 1L)
                .claim("role", "USER")
                .issuedAt(toDate(LocalDateTime.of(2026, 7, 10, 10, 0)))
                .expiration(toDate(LocalDateTime.of(2026, 7, 10, 10, 30)))
                .compact();

        assertThatThrownBy(() -> tokenService.validateAccessToken(accessToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 Access Token입니다.");
    }

    @Test
    @DisplayName("sessionType claim 값이 잘못되면 예외가 발생한다")
    void validateAccessTokenWithInvalidSessionType() {
        String accessToken = createToken(JWT_SECRET)
                .subject("user:1")
                .claim("sessionId", 100L)
                .claim("sessionType", "INVALID")
                .claim("userId", 1L)
                .claim("role", "USER")
                .issuedAt(toDate(LocalDateTime.of(2026, 7, 10, 10, 0)))
                .expiration(toDate(LocalDateTime.of(2026, 7, 10, 10, 30)))
                .compact();

        assertThatThrownBy(() -> tokenService.validateAccessToken(accessToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 Access Token입니다.");
    }

    @Test
    @DisplayName("회원 Access Token에 userId claim이 없으면 예외가 발생한다")
    void validateUserAccessTokenWithoutUserId() {
        String accessToken = createToken(JWT_SECRET)
                .subject("user:1")
                .claim("sessionId", 100L)
                .claim("sessionType", "USER")
                .claim("role", "USER")
                .issuedAt(toDate(LocalDateTime.of(2026, 7, 10, 10, 0)))
                .expiration(toDate(LocalDateTime.of(2026, 7, 10, 10, 30)))
                .compact();

        assertThatThrownBy(() -> tokenService.validateAccessToken(accessToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 Access Token입니다.");
    }

    @Test
    @DisplayName("게스트 Access Token에 guestSessionId claim이 없으면 예외가 발생한다")
    void validateGuestAccessTokenWithoutGuestSessionId() {
        String accessToken = createToken(JWT_SECRET)
                .subject("guest:10")
                .claim("sessionId", 200L)
                .claim("sessionType", "GUEST")
                .issuedAt(toDate(LocalDateTime.of(2026, 7, 10, 10, 0)))
                .expiration(toDate(LocalDateTime.of(2026, 7, 10, 10, 30)))
                .compact();

        assertThatThrownBy(() -> tokenService.validateAccessToken(accessToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 Access Token입니다.");
    }

    @Test
    @DisplayName("회원 Access Token의 sub와 userId가 다르면 예외가 발생한다")
    void validateUserAccessTokenWithMismatchedSubject() {
        String accessToken = createToken(JWT_SECRET)
                .subject("user:2")
                .claim("sessionId", 100L)
                .claim("sessionType", "USER")
                .claim("userId", 1L)
                .claim("role", "USER")
                .issuedAt(toDate(LocalDateTime.of(2026, 7, 10, 10, 0)))
                .expiration(toDate(LocalDateTime.of(2026, 7, 10, 10, 30)))
                .compact();

        assertThatThrownBy(() -> tokenService.validateAccessToken(accessToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 Access Token입니다.");
    }

    @Test
    @DisplayName("게스트 Access Token의 sub와 guestSessionId가 다르면 예외가 발생한다")
    void validateGuestAccessTokenWithMismatchedSubject() {
        String accessToken = createToken(JWT_SECRET)
                .subject("guest:11")
                .claim("sessionId", 200L)
                .claim("sessionType", "GUEST")
                .claim("guestSessionId", 10L)
                .issuedAt(toDate(LocalDateTime.of(2026, 7, 10, 10, 0)))
                .expiration(toDate(LocalDateTime.of(2026, 7, 10, 10, 30)))
                .compact();

        assertThatThrownBy(() -> tokenService.validateAccessToken(accessToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 Access Token입니다.");
    }

    private Claims parseClaims(String accessToken) {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();
    }

    private io.jsonwebtoken.JwtBuilder createToken(String secret) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .signWith(key, Jwts.SIG.HS256);
    }

    private Date toDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
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
