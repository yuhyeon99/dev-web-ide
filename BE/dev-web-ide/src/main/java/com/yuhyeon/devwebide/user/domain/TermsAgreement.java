package com.yuhyeon.devwebide.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "terms_agreements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class TermsAgreement {

    /**
     * 약관 동의 PK
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 약관에 동의한 사용자
     *
     * 한 사용자가 약관 개정 등에 따라
     * 여러 번 동의 이력을 가질 수 있으므로 N:1 관계로 설정
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 이용약관 동의 여부
     */
    @Column(name = "terms_agreed", nullable = false)
    private boolean termsAgreed;

    /**
     * 개인정보 처리방침 동의 여부
     */
    @Column(name = "privacy_agreed", nullable = false)
    private boolean privacyAgreed;

    /**
     * 약관 동의 일시
     */
    @CreatedDate
    @Column(name = "agreed_at", nullable = false, updatable = false)
    private LocalDateTime agreedAt;

    /**
     * TermsAgreement 생성자
     *
     * @param user 약관에 동의한 사용자
     * @param termsAgreed 이용약관 동의 여부
     * @param privacyAgreed 개인정보 처리방침 동의 여부
     */
    @Builder
    public TermsAgreement(
            User user,
            boolean termsAgreed,
            boolean privacyAgreed
    ) {
        this.user = user;
        this.termsAgreed = termsAgreed;
        this.privacyAgreed = privacyAgreed;
    }
}
