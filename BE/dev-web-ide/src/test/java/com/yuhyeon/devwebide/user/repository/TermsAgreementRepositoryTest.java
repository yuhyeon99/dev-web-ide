package com.yuhyeon.devwebide.user.repository;

import com.yuhyeon.devwebide.user.domain.TermsAgreement;
import com.yuhyeon.devwebide.user.domain.User;
import com.yuhyeon.devwebide.user.domain.UserRole;
import com.yuhyeon.devwebide.user.domain.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TermsAgreementRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TermsAgreementRepository termsAgreementRepository;

    @Test
    @DisplayName("약관 동의 저장")
    void saveTermsAgreement() {
        User user = User.builder()
                .email("terms@test.com")
                .nickname("약관테스터")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        TermsAgreement termsAgreement = TermsAgreement.builder()
                .user(savedUser)
                .termsAgreed(true)
                .privacyAgreed(true)
                .build();

        TermsAgreement savedTermsAgreement =
                termsAgreementRepository.save(termsAgreement);

        assertThat(savedTermsAgreement.getId()).isNotNull();
        assertThat(savedTermsAgreement.getUser().getId())
                .isEqualTo(savedUser.getId());
        assertThat(savedTermsAgreement.isTermsAgreed()).isTrue();
        assertThat(savedTermsAgreement.isPrivacyAgreed()).isTrue();
        assertThat(savedTermsAgreement.getAgreedAt()).isNotNull();
    }

    @Test
    @DisplayName("사용자 ID로 약관 동의 이력 조회")
    void findByUserId() {
        User user = User.builder()
                .email("history@test.com")
                .nickname("이력테스터")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        TermsAgreement firstAgreement = TermsAgreement.builder()
                .user(savedUser)
                .termsAgreed(true)
                .privacyAgreed(true)
                .build();

        TermsAgreement secondAgreement = TermsAgreement.builder()
                .user(savedUser)
                .termsAgreed(true)
                .privacyAgreed(true)
                .build();

        termsAgreementRepository.save(firstAgreement);
        termsAgreementRepository.save(secondAgreement);

        List<TermsAgreement> result =
                termsAgreementRepository.findByUserId(savedUser.getId());

        assertThat(result).hasSize(2);
        assertThat(result)
                .allMatch(TermsAgreement::isTermsAgreed)
                .allMatch(TermsAgreement::isPrivacyAgreed);
    }

    @Test
    @DisplayName("사용자의 최신 약관 동의 이력 조회")
    void findTopByUserIdOrderByAgreedAtDesc() {
        User user = User.builder()
                .email("latest@test.com")
                .nickname("최신테스터")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        TermsAgreement firstAgreement = TermsAgreement.builder()
                .user(savedUser)
                .termsAgreed(true)
                .privacyAgreed(true)
                .build();

        TermsAgreement secondAgreement = TermsAgreement.builder()
                .user(savedUser)
                .termsAgreed(true)
                .privacyAgreed(true)
                .build();

        termsAgreementRepository.save(firstAgreement);
        TermsAgreement savedSecondAgreement =
                termsAgreementRepository.save(secondAgreement);

        Optional<TermsAgreement> result =
                termsAgreementRepository.findTopByUserIdOrderByAgreedAtDesc(
                        savedUser.getId()
                );

        assertThat(result).isPresent();
        assertThat(result.get().getId())
                .isEqualTo(savedSecondAgreement.getId());
    }

    @Test
    @DisplayName("필수 약관 전체 동의 여부 확인")
    void existsByUserIdAndTermsAgreedTrueAndPrivacyAgreedTrue() {
        User user = User.builder()
                .email("required@test.com")
                .nickname("필수약관테스터")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        TermsAgreement termsAgreement = TermsAgreement.builder()
                .user(savedUser)
                .termsAgreed(true)
                .privacyAgreed(true)
                .build();

        termsAgreementRepository.save(termsAgreement);

        boolean exists =
                termsAgreementRepository
                        .existsByUserIdAndTermsAgreedTrueAndPrivacyAgreedTrue(
                                savedUser.getId()
                        );

        assertThat(exists).isTrue();
    }
}