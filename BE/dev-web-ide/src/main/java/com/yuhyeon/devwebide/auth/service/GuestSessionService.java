package com.yuhyeon.devwebide.auth.service;

import com.yuhyeon.devwebide.auth.dto.AuthTokenRefreshResponse;
import com.yuhyeon.devwebide.auth.dto.GuestSessionCreateResponse;
import com.yuhyeon.devwebide.user.domain.AuthSession;
import com.yuhyeon.devwebide.user.domain.GuestSession;
import com.yuhyeon.devwebide.user.repository.AuthSessionRepository;
import com.yuhyeon.devwebide.user.repository.GuestSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuestSessionService {

    private static final long GUEST_SESSION_EXPIRATION_DAYS = 7L;

    private final GuestSessionRepository guestSessionRepository;
    private final AuthSessionRepository authSessionRepository;
    private final TokenService tokenService;

    @Transactional
    public GuestSessionCreateResponse createGuestSession(String clientIp) {
        LocalDateTime now = LocalDateTime.now();
        GuestSession guestSession = GuestSession.builder()
                .guestToken(UUID.randomUUID().toString())
                .clientIp(clientIp)
                .expiresAt(now.plusDays(GUEST_SESSION_EXPIRATION_DAYS))
                .build();

        GuestSession savedGuestSession = guestSessionRepository.save(guestSession);
        String refreshToken = tokenService.createRefreshToken();

        AuthSession authSession = AuthSession.builder()
                .user(null)
                .guestSession(savedGuestSession)
                .refreshTokenHash(tokenService.hashRefreshToken(refreshToken))
                .clientIp(clientIp)
                .userAgent(null)
                .expiresAt(tokenService.calculateRefreshTokenExpiresAt(now))
                .build();

        AuthSession savedAuthSession = authSessionRepository.save(authSession);
        AuthTokenRefreshResponse tokenResponse = AuthTokenRefreshResponse.of(
                tokenService.createAccessToken(savedAuthSession, now),
                tokenService.getAccessTokenExpiresInSeconds(),
                tokenService.calculateAccessTokenExpiresAt(now)
        );

        return GuestSessionCreateResponse.of(savedGuestSession, tokenResponse);
    }
}
