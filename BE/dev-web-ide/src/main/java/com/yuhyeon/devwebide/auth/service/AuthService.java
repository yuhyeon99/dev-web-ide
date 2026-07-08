package com.yuhyeon.devwebide.auth.service;

import com.yuhyeon.devwebide.auth.dto.AuthLogoutResponse;
import com.yuhyeon.devwebide.user.domain.AuthSession;
import com.yuhyeon.devwebide.user.repository.AuthSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final String SHA_256 = "SHA-256";

    private final AuthSessionRepository authSessionRepository;

    @Transactional
    public AuthLogoutResponse logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh Token이 필요합니다.");
        }

        String refreshTokenHash = hashRefreshToken(refreshToken);
        AuthSession authSession = authSessionRepository.findByRefreshTokenHash(refreshTokenHash)
                .orElseThrow(() -> new IllegalArgumentException("인증 세션을 찾을 수 없습니다."));

        if (authSession.isRevoked()) {
            return AuthLogoutResponse.of(true, authSession.getRevokedAt());
        }

        LocalDateTime revokedAt = LocalDateTime.now();
        authSession.revoke(revokedAt);

        return AuthLogoutResponse.of(true, revokedAt);
    }

    private String hashRefreshToken(String refreshToken) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(SHA_256);
            byte[] digest = messageDigest.digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Refresh Token 해시를 생성할 수 없습니다.", e);
        }
    }
}
