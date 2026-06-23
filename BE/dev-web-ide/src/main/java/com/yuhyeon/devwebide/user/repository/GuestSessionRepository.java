package com.yuhyeon.devwebide.user.repository;

import com.yuhyeon.devwebide.user.domain.GuestSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface GuestSessionRepository extends JpaRepository<GuestSession, Long> {

    /**
     * 게스트 토큰으로 게스트 세션 조회
     */
    Optional<GuestSession> findByGuestToken(String guestToken);

    /**
     * 게스트 토큰 존재 여부 확인
     */
    boolean existsByGuestToken(String guestToken);

    /**
     * 만료되지 않은 게스트 세션 조회
     *
     * expiresAt이 현재 시간보다 이후인 경우만 조회
     */
    Optional<GuestSession> findByGuestTokenAndExpiresAtAfter(
            String guestToken,
            LocalDateTime now
    );
}