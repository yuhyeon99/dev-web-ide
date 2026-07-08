package com.yuhyeon.devwebide.project.dto;

import com.yuhyeon.devwebide.project.domain.ProjectMember;
import com.yuhyeon.devwebide.project.domain.ProjectMemberRole;
import com.yuhyeon.devwebide.project.domain.ProjectMemberStatus;

import java.time.LocalDateTime;

public record ProjectMemberManageResponse(
        Long projectMemberId,
        Long userId,
        String nickname,
        ProjectMemberRole role,
        ProjectMemberStatus status,
        LocalDateTime invitedAt,
        LocalDateTime joinedAt
) {

    public static ProjectMemberManageResponse from(ProjectMember projectMember) {
        return new ProjectMemberManageResponse(
                projectMember.getId(),
                projectMember.getUser().getId(),
                projectMember.getUser().getNickname(),
                projectMember.getRole(),
                projectMember.getStatus(),
                projectMember.getInvitedAt(),
                projectMember.getJoinedAt()
        );
    }
}
