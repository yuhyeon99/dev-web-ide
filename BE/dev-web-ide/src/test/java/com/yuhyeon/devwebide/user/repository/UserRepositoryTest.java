package com.yuhyeon.devwebide.user.repository;

import com.yuhyeon.devwebide.user.domain.User;
import com.yuhyeon.devwebide.user.domain.UserRole;
import com.yuhyeon.devwebide.user.domain.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("User 저장")
    void saveUser() {
        User user = User.builder()
                .email("test@test.com")
                .nickname("테스터")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@test.com");
        assertThat(savedUser.getNickname()).isEqualTo("테스터");
    }

    @Test
    @DisplayName("이메일로 User 조회")
    void findByEmail() {
        User user = User.builder()
                .email("find@test.com")
                .nickname("조회테스터")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);

        Optional<User> result = userRepository.findByEmail("find@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("find@test.com");
    }

    @Test
    @DisplayName("이메일 존재 여부 확인")
    void existsByEmail() {
        User user = User.builder()
                .email("exists@test.com")
                .nickname("존재테스터")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("exists@test.com");
        boolean notExists = userRepository.existsByEmail("none@test.com");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("활성 회원을 닉네임 또는 이메일로 검색하고 대상 ID는 제외한다")
    void searchActiveUsersByNicknameOrEmail() {
        User owner = User.builder()
                .email("owner@test.com")
                .nickname("owner")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
        User activeByNickname = User.builder()
                .email("member@test.com")
                .nickname("memberKim")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
        User activeByEmail = User.builder()
                .email("kim-email@test.com")
                .nickname("another")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
        User inactive = User.builder()
                .email("inactive-kim@test.com")
                .nickname("inactiveKim")
                .role(UserRole.USER)
                .status(UserStatus.INACTIVE)
                .build();

        User savedOwner = userRepository.save(owner);
        userRepository.save(activeByNickname);
        userRepository.save(activeByEmail);
        userRepository.save(inactive);

        List<User> result =
                userRepository.findByStatusAndIdNotAndNicknameContainingIgnoreCaseOrStatusAndIdNotAndEmailContainingIgnoreCase(
                        UserStatus.ACTIVE,
                        savedOwner.getId(),
                        "kim",
                        UserStatus.ACTIVE,
                        savedOwner.getId(),
                        "kim",
                        PageRequest.of(0, 10)
                );

        assertThat(result)
                .extracting(User::getEmail)
                .containsExactlyInAnyOrder(
                        "member@test.com",
                        "kim-email@test.com"
                );
    }
}
