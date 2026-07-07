package com.yuhyeon.devwebide.user.repository;

import com.yuhyeon.devwebide.user.domain.TermsAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TermsAgreementRepository extends JpaRepository<TermsAgreement, Long> {
    /**
     * 특정 사용자의 약관 동의 이력 조회
     */
    List<TermsAgreement> findByUserId(Long userId);

    /**
     * 특정 사용자의 최신 약관 동의 이력 조회
     */
    Optional<TermsAgreement> findTopByUserIdOrderByAgreedAtDescIdDesc(Long userId);

    /**
     * 특정 사용자가 필수 약관에 모두 동의했는지 확인
     */
    boolean existsByUserIdAndTermsAgreedTrueAndPrivacyAgreedTrue(Long userId);
}
