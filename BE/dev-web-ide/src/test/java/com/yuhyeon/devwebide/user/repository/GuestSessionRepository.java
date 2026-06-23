package com.yuhyeon.devwebide.user.repository;

import com.yuhyeon.devwebide.user.domain.GuestSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class GuestSessionRepositoryTest {

    @Autowired
    private GuestSessionRepository guestSessionRepository;

    @Test
    @DisplayName("게스트 세션 저장")
    void saveGuestSession() {
        GuestSession guestSession = GuestSession.builder()
                .guestToken("guest-token-save")
                .clientIp("127.0.0.1")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        GuestSession savedGuestSession =
                guestSessionRepository.save(guestSession);

        assertThat(savedGuestSession.getId()).isNotNull();
        assertThat(savedGuestSession.getGuestToken())
                .isEqualTo("guest-token-save");
        assertThat(savedGuestSession.getClientIp())
                .isEqualTo("127.0.0.1");
        assertThat(savedGuestSession.getExpiresAt()).isNotNull();
        assertThat(savedGuestSession.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("guestToken으로 게스트 세션 조회")
    void findByGuestToken() {
        GuestSession guestSession = GuestSession.builder()
                .guestToken("guest-token-find")
                .clientIp("127.0.0.1")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        guestSessionRepository.save(guestSession);

        Optional<GuestSession> result =
                guestSessionRepository.findByGuestToken("guest-token-find");

        assertThat(result).isPresent();
        assertThat(result.get().getGuestToken())
                .isEqualTo("guest-token-find");
        assertThat(result.get().getClientIp())
                .isEqualTo("127.0.0.1");
    }

    @Test
    @DisplayName("guestToken 존재 여부 확인")
    void existsByGuestToken() {
        GuestSession guestSession = GuestSession.builder()
                .guestToken("guest-token-exists")
                .clientIp("127.0.0.1")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        guestSessionRepository.save(guestSession);

        boolean exists =
                guestSessionRepository.existsByGuestToken("guest-token-exists");

        boolean notExists =
                guestSessionRepository.existsByGuestToken("guest-token-none");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("만료되지 않은 guestToken으로 게스트 세션 조회")
    void findByGuestTokenAndExpiresAtAfter() {
        LocalDateTime now = LocalDateTime.now();

        GuestSession guestSession = GuestSession.builder()
                .guestToken("guest-token-valid")
                .clientIp("127.0.0.1")
                .expiresAt(now.plusDays(7))
                .build();

        guestSessionRepository.save(guestSession);

        Optional<GuestSession> result =
                guestSessionRepository.findByGuestTokenAndExpiresAtAfter(
                        "guest-token-valid",
                        now
                );

        assertThat(result).isPresent();
        assertThat(result.get().getGuestToken())
                .isEqualTo("guest-token-valid");
    }

    @Test
    @DisplayName("만료된 guestToken은 만료 전 조회에서 조회되지 않음")
    void findByGuestTokenAndExpiresAtAfterExpired() {
        LocalDateTime now = LocalDateTime.now();

        GuestSession guestSession = GuestSession.builder()
                .guestToken("guest-token-expired")
                .clientIp("127.0.0.1")
                .expiresAt(now.minusDays(1))
                .build();

        guestSessionRepository.save(guestSession);

        Optional<GuestSession> result =
                guestSessionRepository.findByGuestTokenAndExpiresAtAfter(
                        "guest-token-expired",
                        now
                );

        assertThat(result).isEmpty();
    }
}