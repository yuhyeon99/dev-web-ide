package com.yuhyeon.devwebide.user.dto;

import com.yuhyeon.devwebide.user.domain.User;

public record UserSearchResponse(
        Long userId,
        String email,
        String nickname
) {

    public static UserSearchResponse from(User user) {
        return new UserSearchResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname()
        );
    }
}
