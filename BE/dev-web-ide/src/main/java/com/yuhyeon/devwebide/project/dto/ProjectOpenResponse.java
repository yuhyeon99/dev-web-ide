package com.yuhyeon.devwebide.project.dto;

import com.yuhyeon.devwebide.project.domain.Project;
import com.yuhyeon.devwebide.project.domain.ProjectStatus;
import com.yuhyeon.devwebide.project.domain.ProjectType;
import com.yuhyeon.devwebide.project.domain.ProjectVisibility;
import com.yuhyeon.devwebide.runtime.domain.Runtime;
import com.yuhyeon.devwebide.runtime.domain.RuntimeLanguage;

import java.time.LocalDateTime;

/**
 * 프로젝트 열기 응답 DTO
 *
 * 프로젝트를 연 뒤 워크스페이스 진입에 필요한 기본 정보를 반환합니다.
 * 실제 프로젝트 접근 기록은 ProjectAccessLog에 저장합니다.
 */
public record ProjectOpenResponse(
        Long projectId,
        String name,
        String description,
        ProjectType projectType,
        ProjectVisibility visibility,
        ProjectStatus status,
        Long runtimeId,
        String runtimeName,
        String runtimeDisplayName,
        RuntimeLanguage runtimeLanguage,
        LocalDateTime openedAt
) {

    /**
     * Project 엔티티와 프로젝트 열기 시간을 응답 DTO로 변환합니다.
     *
     * @param project 열린 프로젝트
     * @param openedAt 프로젝트를 연 시간
     * @return 프로젝트 열기 응답 DTO
     */
    public static ProjectOpenResponse from(
            Project project,
            LocalDateTime openedAt
    ) {
        Runtime runtime = project.getRuntime();

        return new ProjectOpenResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getProjectType(),
                project.getVisibility(),
                project.getStatus(),
                runtime.getId(),
                runtime.getName(),
                runtime.getDisplayName(),
                runtime.getLanguage(),
                openedAt
        );
    }
}