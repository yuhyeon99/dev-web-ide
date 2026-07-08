package com.yuhyeon.devwebide.project.dto;

import com.yuhyeon.devwebide.project.domain.ProjectMemberRole;
import jakarta.validation.constraints.NotNull;

public record ProjectMemberInviteRequest(
        @NotNull(message = "초대할 사용자 ID는 필수입니다.")
        Long userId,

        @NotNull(message = "프로젝트 멤버 권한은 필수입니다.")
        ProjectMemberRole role
) {
}
