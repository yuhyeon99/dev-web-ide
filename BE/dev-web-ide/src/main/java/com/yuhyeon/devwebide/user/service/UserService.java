package com.yuhyeon.devwebide.user.service;

import com.yuhyeon.devwebide.auth.dto.AuthenticatedPrincipal;
import com.yuhyeon.devwebide.user.domain.User;
import com.yuhyeon.devwebide.user.domain.UserStatus;
import com.yuhyeon.devwebide.user.dto.UserMeResponse;
import com.yuhyeon.devwebide.user.dto.UserProfileUpdateRequest;
import com.yuhyeon.devwebide.user.dto.UserSearchResponse;
import com.yuhyeon.devwebide.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserMeResponse getCurrentUser(AuthenticatedPrincipal principal) {
        User user = getActiveUser(principal);

        return UserMeResponse.from(user);
    }

    public List<UserSearchResponse> searchInvitableUsers(
            AuthenticatedPrincipal principal,
            String query
    ) {
        User currentUser = getActiveUser(principal);
        String normalizedQuery = query == null ? "" : query.trim();

        if (normalizedQuery.length() < 2) {
            return List.of();
        }

        return userRepository
                .findByStatusAndIdNotAndNicknameContainingIgnoreCaseOrStatusAndIdNotAndEmailContainingIgnoreCase(
                        UserStatus.ACTIVE,
                        currentUser.getId(),
                        normalizedQuery,
                        UserStatus.ACTIVE,
                        currentUser.getId(),
                        normalizedQuery,
                        PageRequest.of(0, 10)
                )
                .stream()
                .map(UserSearchResponse::from)
                .toList();
    }

    @Transactional
    public UserMeResponse updateCurrentUserProfile(
            AuthenticatedPrincipal principal,
            UserProfileUpdateRequest request
    ) {
        User user = getActiveUser(principal);

        user.updateNickname(request.nickname());

        return UserMeResponse.from(user);
    }

    private User getActiveUser(AuthenticatedPrincipal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("인증 정보가 필요합니다.");
        }

        if (!principal.isUserSession()) {
            throw new IllegalArgumentException("회원 Access Token이 필요합니다.");
        }

        User user = userRepository.findById(principal.userId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("활성 상태의 사용자가 아닙니다.");
        }

        return user;
    }
}
