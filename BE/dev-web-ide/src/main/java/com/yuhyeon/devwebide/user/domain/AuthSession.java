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
 * 인증 세션 엔티티
 *
 * 회원 사용자와 게스트 사용자의 로그인 상태를 관리합니다.
 * Refresh Token 해시, 만료 일시, 로그아웃 여부를 저장합니다.
 */
@Entity
@Table(name = "auth_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class AuthSession {

    /**
     * 인증 세션 PK
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 회원 사용자
     *
     * 회원 로그인 세션인 경우 값이 존재합니다.
     * 게스트 세션인 경우 null입니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 게스트 세션
     *
     * 게스트 사용자 세션인 경우 값이 존재합니다.
     * 회원 로그인 세션인 경우 null입니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_session_id") // AuthSession 테이블에 생성될 FK 컬럼 이름을 지정
    private GuestSession guestSession;

    /**
     * Refresh Token 해시
     *
     * 실제 Refresh Token 원문은 저장하지 않고,
     * 해시 값만 저장합니다.
     *
     * 단일 컬럼 유니크 제약이므로
     * @Table의 uniqueConstraints 대신
     * @Column(unique = true)로 설정합니다.
     */
    @Column(
            name = "refresh_token_hash",
            nullable = false,
            unique = true,
            length = 200
    )
    private String refreshTokenHash;

    /**
     * 접속 IP
     *
     * IPv4, IPv6를 모두 고려하여 최대 45자로 설정합니다.
     */
    @Column(name = "client_ip", length = 45)
    private String clientIp;

    /**
     * 사용자 브라우저 / 클라이언트 정보
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * 세션 만료 일시
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
     * 세션 폐기 일시
     *
     * 로그아웃 또는 강제 만료 처리 시 값이 들어갑니다.
     * null이면 아직 폐기되지 않은 세션입니다.
     */
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    /**
     * AuthSession 생성자
     *
     * 회원 세션은 user만 가져야 하고,
     * 게스트 세션은 guestSession만 가져야 합니다.
     *
     * @param user 회원 사용자
     * @param guestSession 게스트 세션
     * @param refreshTokenHash Refresh Token 해시
     * @param clientIp 접속 IP
     * @param userAgent 사용자 브라우저 / 클라이언트 정보
     * @param expiresAt 세션 만료 일시
     */
    @Builder
    public AuthSession(
            User user,
            GuestSession guestSession,
            String refreshTokenHash,
            String clientIp,
            String userAgent,
            LocalDateTime expiresAt
    ) {
        validateSessionOwner(user, guestSession);

        this.user = user;
        this.guestSession = guestSession;
        this.refreshTokenHash = refreshTokenHash;
        this.clientIp = clientIp;
        this.userAgent = userAgent;
        this.expiresAt = expiresAt;
    }

    /**
     * 회원 세션 또는 게스트 세션 중 하나만 지정되었는지 검증합니다.
     *
     * @param user 회원 사용자
     * @param guestSession 게스트 세션
     */
    private void validateSessionOwner(User user, GuestSession guestSession) {
        boolean hasUser = user != null;
        boolean hasGuestSession = guestSession != null;

        if (hasUser == hasGuestSession) {
            throw new IllegalArgumentException(
                    "회원 세션 또는 게스트 세션 중 하나만 지정해야 합니다."
            );
        }
    }

    /**
     * 회원 세션 여부 확인
     *
     * @return 회원 세션 여부
     */
    public boolean isUserSession() {
        return user != null;
    }

    /**
     * 게스트 세션 여부 확인
     *
     * @return 게스트 세션 여부
     */
    public boolean isGuestSession() {
        return guestSession != null;
    }

    /**
     * 세션 만료 여부 확인
     *
     * @param now 현재 시간
     * @return 만료 여부
     */
    public boolean isExpired(LocalDateTime now) {
        return expiresAt.isBefore(now) || expiresAt.isEqual(now);
    }

    /**
     * 세션 폐기 여부 확인
     *
     * @return 폐기 여부
     */
    public boolean isRevoked() {
        return revokedAt != null;
    }

    /**
     * 활성 세션 여부 확인
     *
     * @param now 현재 시간
     * @return 활성 여부
     */
    public boolean isActive(LocalDateTime now) {
        return !isExpired(now) && !isRevoked();
    }

    /**
     * 세션 폐기 처리
     *
     * 로그아웃 시 사용합니다.
     *
     * @param revokedAt 폐기 일시
     */
    public void revoke(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }
}