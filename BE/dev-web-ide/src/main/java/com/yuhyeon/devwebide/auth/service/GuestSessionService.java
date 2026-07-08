package com.yuhyeon.devwebide.auth.service;

import com.yuhyeon.devwebide.auth.dto.GuestSessionCreateResponse;
import com.yuhyeon.devwebide.user.domain.GuestSession;
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

    @Transactional
    public GuestSessionCreateResponse createGuestSession(String clientIp) {
        GuestSession guestSession = GuestSession.builder()
                .guestToken(UUID.randomUUID().toString())
                .clientIp(clientIp)
                .expiresAt(LocalDateTime.now().plusDays(GUEST_SESSION_EXPIRATION_DAYS))
                .build();

        GuestSession savedGuestSession = guestSessionRepository.save(guestSession);

        return GuestSessionCreateResponse.from(savedGuestSession);
    }
}
