package com.yuhyeon.devwebide.user.service;

import com.yuhyeon.devwebide.auth.dto.AuthenticatedPrincipal;
import com.yuhyeon.devwebide.user.domain.User;
import com.yuhyeon.devwebide.user.domain.UserRole;
import com.yuhyeon.devwebide.user.domain.UserStatus;
import com.yuhyeon.devwebide.user.dto.UserMeResponse;
import com.yuhyeon.devwebide.user.dto.UserProfileUpdateRequest;
import com.yuhyeon.devwebide.user.dto.UserSearchResponse;
import com.yuhyeon.devwebide.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원 Principal로 현재 사용자를 조회한다")
    void getCurrentUser() {
        AuthenticatedPrincipal principal = createUserPrincipal(1L);
        User user = createUser(1L, UserStatus.ACTIVE);

        given(userRepository.findById(1L))
                .willReturn(Optional.of(user));

        UserMeResponse response = userService.getCurrentUser(principal);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("user@test.com");
        assertThat(response.nickname()).isEqualTo("user");
        assertThat(response.role()).isEqualTo(UserRole.USER);
        assertThat(response.status()).isEqualTo(UserStatus.ACTIVE);

        then(userRepository).should()
                .findById(1L);
    }

    @Test
    @DisplayName("현재 사용자의 닉네임을 수정한다")
    void updateCurrentUserProfile() {
        AuthenticatedPrincipal principal = createUserPrincipal(1L);
        User user = createUser(1L, UserStatus.ACTIVE);

        given(userRepository.findById(1L))
                .willReturn(Optional.of(user));

        UserMeResponse response = userService.updateCurrentUserProfile(
                principal,
                new UserProfileUpdateRequest("새닉네임")
        );

        assertThat(response.nickname()).isEqualTo("새닉네임");
        assertThat(user.getNickname()).isEqualTo("새닉네임");
    }

    @Test
    @DisplayName("초대 가능한 활성 회원을 검색한다")
    void searchInvitableUsers() {
        AuthenticatedPrincipal principal = createUserPrincipal(1L);
        User currentUser = createUser(1L, UserStatus.ACTIVE);
        User targetUser = createUser(2L, UserStatus.ACTIVE);
        ReflectionTestUtils.setField(targetUser, "email", "target@test.com");
        ReflectionTestUtils.setField(targetUser, "nickname", "target");

        given(userRepository.findById(1L))
                .willReturn(Optional.of(currentUser));
        given(userRepository.findByStatusAndIdNotAndNicknameContainingIgnoreCaseOrStatusAndIdNotAndEmailContainingIgnoreCase(
                org.mockito.ArgumentMatchers.eq(UserStatus.ACTIVE),
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq("ta"),
                org.mockito.ArgumentMatchers.eq(UserStatus.ACTIVE),
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq("ta"),
                org.mockito.ArgumentMatchers.any()
        )).willReturn(List.of(targetUser));

        List<UserSearchResponse> responses =
                userService.searchInvitableUsers(principal, " ta ");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).userId()).isEqualTo(2L);
        assertThat(responses.get(0).email()).isEqualTo("target@test.com");
        assertThat(responses.get(0).nickname()).isEqualTo("target");
    }

    @Test
    @DisplayName("검색어가 2글자 미만이면 빈 결과를 반환한다")
    void searchInvitableUsersWithShortQuery() {
        AuthenticatedPrincipal principal = createUserPrincipal(1L);
        User currentUser = createUser(1L, UserStatus.ACTIVE);

        given(userRepository.findById(1L))
                .willReturn(Optional.of(currentUser));

        List<UserSearchResponse> responses =
                userService.searchInvitableUsers(principal, "t");

        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("Principal이 null이면 예외가 발생한다")
    void getCurrentUserWithNullPrincipal() {
        assertThatThrownBy(() -> userService.getCurrentUser(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("인증 정보가 필요합니다.");
    }

    @Test
    @DisplayName("게스트 Principal이면 예외가 발생한다")
    void getCurrentUserWithGuestPrincipal() {
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
                200L,
                "GUEST",
                null,
                10L,
                null
        );

        assertThatThrownBy(() -> userService.getCurrentUser(principal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 Access Token이 필요합니다.");
    }

    @Test
    @DisplayName("userId에 해당하는 User가 없으면 예외가 발생한다")
    void getCurrentUserWithNotFoundUser() {
        AuthenticatedPrincipal principal = createUserPrincipal(1L);

        given(userRepository.findById(1L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCurrentUser(principal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("User가 INACTIVE이면 예외가 발생한다")
    void getCurrentUserWithInactiveUser() {
        assertInactiveUserStatus(UserStatus.INACTIVE);
    }

    @Test
    @DisplayName("User가 DELETED이면 예외가 발생한다")
    void getCurrentUserWithDeletedUser() {
        assertInactiveUserStatus(UserStatus.DELETED);
    }

    private void assertInactiveUserStatus(UserStatus userStatus) {
        AuthenticatedPrincipal principal = createUserPrincipal(1L);
        User user = createUser(1L, userStatus);

        given(userRepository.findById(1L))
                .willReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.getCurrentUser(principal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("활성 상태의 사용자가 아닙니다.");
    }

    private AuthenticatedPrincipal createUserPrincipal(Long userId) {
        return new AuthenticatedPrincipal(
                100L,
                "USER",
                userId,
                null,
                "USER"
        );
    }

    private User createUser(Long id, UserStatus status) {
        User user = User.builder()
                .email("user@test.com")
                .nickname("user")
                .role(UserRole.USER)
                .status(status)
                .build();
        ReflectionTestUtils.setField(user, "id", id);

        return user;
    }
}
