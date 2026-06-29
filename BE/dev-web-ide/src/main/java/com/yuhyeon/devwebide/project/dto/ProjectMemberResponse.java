package com.yuhyeon.devwebide.project.dto;

import com.yuhyeon.devwebide.project.domain.ProjectMember;
import com.yuhyeon.devwebide.project.domain.ProjectMemberRole;
import com.yuhyeon.devwebide.project.domain.ProjectMemberStatus;

import java.time.LocalDateTime;

/**
 * 프로젝트 멤버 응답 DTO
 *
 * 프로젝트 상세 조회 시 참여 멤버와 권한 정보를 반환합니다.
 */
public record ProjectMemberResponse(
        Long userId,
        String nickname,
        ProjectMemberRole role,
        ProjectMemberStatus status,
        LocalDateTime joinedAt
) {

    /**
     * ProjectMember 엔티티를 응답 DTO로 변환합니다.
     *
     * @param projectMember 프로젝트 멤버 엔티티
     * @return 프로젝트 멤버 응답 DTO
     */
    public static ProjectMemberResponse from(ProjectMember projectMember) {
        return new ProjectMemberResponse(
                projectMember.getUser().getId(),
                projectMember.getUser().getNickname(),
                projectMember.getRole(),
                projectMember.getStatus(),
                projectMember.getJoinedAt()
        );
    }
}