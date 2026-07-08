package com.yuhyeon.devwebide.auth.service;

import com.yuhyeon.devwebide.auth.dto.GuestSessionCreateResponse;
import com.yuhyeon.devwebide.user.domain.GuestSession;
import com.yuhyeon.devwebide.user.repository.GuestSessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class GuestSessionServiceTest {

    @Mock
    private GuestSessionRepository guestSessionRepository;

    @InjectMocks
    private GuestSessionService guestSessionService;

    @Test
    @DisplayName("게스트 세션을 생성한다")
    void createGuestSession() {
        Long guestSessionId = 1L;
        String clientIp = "127.0.0.1";
        LocalDateTime before = LocalDateTime.now().plusDays(7).minusSeconds(1);

        given(guestSessionRepository.save(any(GuestSession.class)))
                .willAnswer(invocation -> {
                    GuestSession guestSession = invocation.getArgument(0);
                    ReflectionTestUtils.setField(guestSession, "id", guestSessionId);
                    ReflectionTestUtils.setField(
                            guestSession,
                            "createdAt",
                            LocalDateTime.of(2026, 7, 8, 10, 0)
                    );
                    return guestSession;
                });

        GuestSessionCreateResponse response =
                guestSessionService.createGuestSession(clientIp);

        LocalDateTime after = LocalDateTime.now().plusDays(7).plusSeconds(1);

        assertThat(response.guestSessionId()).isEqualTo(guestSessionId);
        assertThat(response.guestToken()).isNotBlank();
        assertThat(response.expiresAt()).isBetween(before, after);
        assertThat(response.createdAt()).isEqualTo(LocalDateTime.of(2026, 7, 8, 10, 0));

        ArgumentCaptor<GuestSession> guestSessionCaptor =
                ArgumentCaptor.forClass(GuestSession.class);

        then(guestSessionRepository).should()
                .save(guestSessionCaptor.capture());

        GuestSession savedGuestSession = guestSessionCaptor.getValue();

        assertThat(savedGuestSession.getGuestToken()).isNotBlank();
        assertThat(savedGuestSession.getClientIp()).isEqualTo(clientIp);
        assertThat(savedGuestSession.getExpiresAt()).isBetween(before, after);
    }
}
