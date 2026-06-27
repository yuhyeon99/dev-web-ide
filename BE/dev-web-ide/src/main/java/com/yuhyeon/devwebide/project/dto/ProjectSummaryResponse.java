package com.yuhyeon.devwebide.project.dto;

import com.yuhyeon.devwebide.project.domain.Project;
import com.yuhyeon.devwebide.project.domain.ProjectStatus;
import com.yuhyeon.devwebide.project.domain.ProjectType;
import com.yuhyeon.devwebide.project.domain.ProjectVisibility;
import com.yuhyeon.devwebide.runtime.domain.RuntimeLanguage;

import java.time.LocalDateTime;

/**
 * 프로젝트 목록 조회 응답 DTO
 *
 * GET /api/projects/my 에서
 * 사용자가 생성한 프로젝트 목록을 반환할 때 사용합니다.
 */
public record ProjectSummaryResponse(
        Long id,
        String name,
        String description,
        ProjectType projectType,
        ProjectVisibility visibility,
        ProjectStatus status,
        Long runtimeId,
        String runtimeName,
        String runtimeDisplayName,
        RuntimeLanguage runtimeLanguage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    /**
     * Project 엔티티를 프로젝트 목록 응답 DTO로 변환합니다.
     *
     * @param project 프로젝트 엔티티
     * @return 프로젝트 목록 응답 DTO
     */
    public static ProjectSummaryResponse from(Project project) {
        return new ProjectSummaryResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getProjectType(),
                project.getVisibility(),
                project.getStatus(),
                project.getRuntime().getId(),
                project.getRuntime().getName(),
                project.getRuntime().getDisplayName(),
                project.getRuntime().getLanguage(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}