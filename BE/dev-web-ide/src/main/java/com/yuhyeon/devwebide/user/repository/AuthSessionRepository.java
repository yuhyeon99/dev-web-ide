package com.yuhyeon.devwebide.user.repository;

import com.yuhyeon.devwebide.user.domain.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AuthSessionRepository extends JpaRepository<AuthSession, Long> {

    /**
     * Refresh Token 해시로 인증 세션 조회
     */
    Optional<AuthSession> findByRefreshTokenHash(String refreshTokenHash);

    /**
     * Refresh Token 해시 존재 여부 확인
     */
    boolean existsByRefreshTokenHash(String refreshTokenHash);

    /**
     * 유효한 Refresh Token 해시로 인증 세션 조회
     *
     * revokedAt이 null이고,
     * expiresAt이 현재 시간보다 이후인 경우만 조회합니다.
     */
    Optional<AuthSession> findByRefreshTokenHashAndRevokedAtIsNullAndExpiresAtAfter(
            String refreshTokenHash,
            LocalDateTime now
    );
}