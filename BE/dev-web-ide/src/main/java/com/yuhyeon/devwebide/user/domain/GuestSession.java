package com.yuhyeon.devwebide.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 게스트 세션 엔티티
 *
 * 로그인하지 않은 게스트 사용자를 식별하기 위한 엔티티입니다.
 * 게스트 프로젝트 생성, 최근 프로젝트 조회, 워크스페이스 접근 등에 사용됩니다.
 */
@Entity
@Table(name = "guest_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class GuestSession {

    /**
     * 게스트 세션 PK
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 게스트 식별 토큰
     *
     * 브라우저 쿠키 또는 헤더를 통해 전달되는 값과 매칭하여
     * 게스트 사용자를 식별합니다.
     */
    @Column(name = "guest_token", nullable = false, unique = true, length = 200)
    private String guestToken;

    /**
     * 접속 IP
     *
     * IPv4, IPv6를 모두 고려하여 최대 45자로 설정합니다.
     */
    @Column(name = "client_ip", length = 45)
    private String clientIp;

    /**
     * 게스트 세션 만료 일시
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * 생성 일시
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * GuestSession 생성자
     *
     * @param guestToken 게스트 식별 토큰
     * @param clientIp 접속 IP
     * @param expiresAt 만료 일시
     */
    @Builder
    public GuestSession(
            String guestToken,
            String clientIp,
            LocalDateTime expiresAt
    ) {
        this.guestToken = guestToken;
        this.clientIp = clientIp;
        this.expiresAt = expiresAt;
    }

    /**
     * 게스트 세션 만료 여부 확인
     *
     * @param now 현재 시간
     * @return 만료 여부
     */
    public boolean isExpired(LocalDateTime now) {
        return expiresAt.isBefore(now) || expiresAt.isEqual(now);
    }
}