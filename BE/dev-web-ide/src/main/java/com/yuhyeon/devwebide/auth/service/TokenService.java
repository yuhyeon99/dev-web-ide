package com.yuhyeon.devwebide.auth.service;

import com.yuhyeon.devwebide.auth.dto.AuthenticatedPrincipal;
import com.yuhyeon.devwebide.user.domain.AuthSession;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.HexFormat;

@Service
public class TokenService {

    private static final String SHA_256 = "SHA-256";
    private static final String USER_SESSION_TYPE = "USER";
    private static final String GUEST_SESSION_TYPE = "GUEST";
    private static final int REFRESH_TOKEN_BYTE_LENGTH = 32;
    private static final long REFRESH_TOKEN_EXPIRATION_DAYS = 14L;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiration-minutes:30}")
    private long accessTokenExpirationMinutes;

    public String hashRefreshToken(String refreshToken) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(SHA_256);
            byte[] digest = messageDigest.digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Refresh Token 해시를 생성할 수 없습니다.", e);
        }
    }

    public String createRefreshToken() {
        byte[] tokenBytes = new byte[REFRESH_TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(tokenBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);
    }

    public String createAccessToken(AuthSession authSession, LocalDateTime issuedAt) {
        LocalDateTime expiresAt = calculateAccessTokenExpiresAt(issuedAt);
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        var builder = Jwts.builder()
                .subject(createSubject(authSession))
                .claim("sessionId", authSession.getId())
                .claim("sessionType", createSessionType(authSession))
                .issuedAt(toDate(issuedAt))
                .expiration(toDate(expiresAt));

        if (authSession.isUserSession()) {
            builder.claim("userId", authSession.getUser().getId())
                    .claim("role", authSession.getUser().getRole().name());
        }

        if (authSession.isGuestSession()) {
            builder.claim("guestSessionId", authSession.getGuestSession().getId());
        }

        return builder.signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public AuthenticatedPrincipal validateAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("Access Token이 필요합니다.");
        }

        Claims claims = parseClaims(accessToken);

        Long sessionId = getRequiredLongClaim(claims, "sessionId");
        String sessionType = getRequiredStringClaim(claims, "sessionType");

        if (USER_SESSION_TYPE.equals(sessionType)) {
            Long userId = getRequiredLongClaim(claims, "userId");
            String role = getRequiredStringClaim(claims, "role");
            validateSubject(claims, "user:" + userId);

            return new AuthenticatedPrincipal(
                    sessionId,
                    sessionType,
                    userId,
                    null,
                    role
            );
        }

        if (GUEST_SESSION_TYPE.equals(sessionType)) {
            Long guestSessionId = getRequiredLongClaim(claims, "guestSessionId");
            validateSubject(claims, "guest:" + guestSessionId);

            return new AuthenticatedPrincipal(
                    sessionId,
                    sessionType,
                    null,
                    guestSessionId,
                    null
            );
        }

        throw new IllegalArgumentException("유효하지 않은 Access Token입니다.");
    }

    public LocalDateTime calculateAccessTokenExpiresAt(LocalDateTime issuedAt) {
        return issuedAt.plusMinutes(accessTokenExpirationMinutes);
    }

    public long getAccessTokenExpiresInSeconds() {
        return accessTokenExpirationMinutes * 60;
    }

    public LocalDateTime calculateRefreshTokenExpiresAt(LocalDateTime issuedAt) {
        return issuedAt.plusDays(REFRESH_TOKEN_EXPIRATION_DAYS);
    }

    public long getRefreshTokenExpiresInSeconds() {
        return REFRESH_TOKEN_EXPIRATION_DAYS * 24 * 60 * 60;
    }

    private String createSubject(AuthSession authSession) {
        if (authSession.isUserSession()) {
            return "user:" + authSession.getUser().getId();
        }

        return "guest:" + authSession.getGuestSession().getId();
    }

    private String createSessionType(AuthSession authSession) {
        if (authSession.isUserSession()) {
            return USER_SESSION_TYPE;
        }

        return GUEST_SESSION_TYPE;
    }

    private Date toDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private Claims parseClaims(String accessToken) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 Access Token입니다.", e);
        }
    }

    private Long getRequiredLongClaim(Claims claims, String claimName) {
        Object value = claims.get(claimName);
        if (value instanceof Number number) {
            return number.longValue();
        }

        throw new IllegalArgumentException("유효하지 않은 Access Token입니다.");
    }

    private String getRequiredStringClaim(Claims claims, String claimName) {
        Object value = claims.get(claimName);
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            return stringValue;
        }

        throw new IllegalArgumentException("유효하지 않은 Access Token입니다.");
    }

    private void validateSubject(Claims claims, String expectedSubject) {
        if (!expectedSubject.equals(claims.getSubject())) {
            throw new IllegalArgumentException("유효하지 않은 Access Token입니다.");
        }
    }
}
