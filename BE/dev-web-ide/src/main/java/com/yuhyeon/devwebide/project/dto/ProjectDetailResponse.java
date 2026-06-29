package com.yuhyeon.devwebide.project.dto;

import com.yuhyeon.devwebide.project.domain.Project;
import com.yuhyeon.devwebide.project.domain.ProjectMember;
import com.yuhyeon.devwebide.project.domain.ProjectSettings;
import com.yuhyeon.devwebide.project.domain.ProjectStatus;
import com.yuhyeon.devwebide.project.domain.ProjectType;
import com.yuhyeon.devwebide.project.domain.ProjectVisibility;
import com.yuhyeon.devwebide.runtime.dto.RuntimeResponse;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 프로젝트 상세 조회 응답 DTO
 *
 * 프로젝트 기본 정보, 런타임, 설정, 멤버 정보를 함께 반환합니다.
 */
public record ProjectDetailResponse(
        Long id,
        String name,
        String description,
        ProjectType projectType,
        ProjectVisibility visibility,
        ProjectStatus status,
        String storagePath,
        RuntimeResponse runtime,
        ProjectSettingsResponse settings,
        List<ProjectMemberResponse> members,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    /**
     * Project 엔티티와 연관 데이터를 응답 DTO로 변환합니다.
     *
     * @param project 프로젝트 엔티티
     * @param projectSettings 프로젝트 설정 엔티티
     * @param projectMembers 프로젝트 멤버 목록
     * @return 프로젝트 상세 응답 DTO
     */
    public static ProjectDetailResponse from(
            Project project,
            ProjectSettings projectSettings,
            List<ProjectMember> projectMembers
    ) {
        return new ProjectDetailResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getProjectType(),
                project.getVisibility(),
                project.getStatus(),
                project.getStoragePath(),
                RuntimeResponse.from(project.getRuntime()),
                ProjectSettingsResponse.from(projectSettings),
                projectMembers.stream()
                        .map(ProjectMemberResponse::from)
                        .toList(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}