package com.yuhyeon.devwebide.user.dto;

import com.yuhyeon.devwebide.user.domain.User;
import com.yuhyeon.devwebide.user.domain.UserRole;
import com.yuhyeon.devwebide.user.domain.UserStatus;

public record UserMeResponse(
        Long userId,
        String email,
        String nickname,
        UserRole role,
        UserStatus status
) {

    public static UserMeResponse from(User user) {
        return new UserMeResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole(),
                user.getStatus()
        );
    }
}
