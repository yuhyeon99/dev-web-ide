package com.yuhyeon.devwebide.auth.service;

import com.yuhyeon.devwebide.user.domain.AuthSession;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HexFormat;

@Service
public class TokenService {

    private static final String SHA_256 = "SHA-256";
    private static final String USER_SESSION_TYPE = "USER";
    private static final String GUEST_SESSION_TYPE = "GUEST";

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

    public LocalDateTime calculateAccessTokenExpiresAt(LocalDateTime issuedAt) {
        return issuedAt.plusMinutes(accessTokenExpirationMinutes);
    }

    public long getAccessTokenExpiresInSeconds() {
        return accessTokenExpirationMinutes * 60;
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
}
