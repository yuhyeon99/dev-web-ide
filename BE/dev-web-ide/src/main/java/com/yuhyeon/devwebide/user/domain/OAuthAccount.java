package com.yuhyeon.devwebide.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * OAuth 계정 엔티티
 *
 * 외부 OAuth 제공자(GitHub, Google, Kakao)의 계정과
 * 내부 User 계정을 연결하기 위한 엔티티
 */
@Entity
@Table(
        name = "oauth_accounts",
        uniqueConstraints = {
                // provider + provider_user_id 조합 중복 방지
                @UniqueConstraint(
                        name = "uk_provider_user",
                        columnNames = {"provider", "provider_user_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 기본 생성자
@EntityListeners(AuditingEntityListener.class) // 생성일 자동 관리
public class OAuthAccount {

    /**
     * OAuth 계정 PK
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 연결된 사용자
     *
     * 여러 OAuth 계정이 하나의 User에 연결될 수 있으므로
     * N:1 관계로 설정
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * OAuth 제공자
     *
     * GITHUB
     * GOOGLE
     * KAKAO
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 30)
    private OAuthProvider provider;

    /**
     * OAuth 제공자 측 사용자 고유 ID
     */
    @Column(name = "provider_user_id", nullable = false, length = 200)
    private String providerUserId;

    /**
     * OAuth 제공자 이메일
     */
    @Column(name = "provider_email", length = 200)
    private String providerEmail;

    /**
     * 생성 일시 (자동 생성)
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * OAuthAccount 생성자
     *
     * @param user 연결할 사용자
     * @param provider OAuth 제공자
     * @param providerUserId OAuth 제공자 측 사용자 ID
     * @param providerEmail OAuth 제공자 이메일
     */
    @Builder
    public OAuthAccount(
            User user,
            OAuthProvider provider,
            String providerUserId,
            String providerEmail
    ) {
        this.user = user;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.providerEmail = providerEmail;
    }
}