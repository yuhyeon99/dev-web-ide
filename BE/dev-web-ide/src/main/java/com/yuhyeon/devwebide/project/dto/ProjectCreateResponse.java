package com.yuhyeon.devwebide.project.dto;

import com.yuhyeon.devwebide.project.domain.Project;
import com.yuhyeon.devwebide.project.domain.ProjectStatus;
import com.yuhyeon.devwebide.project.domain.ProjectType;
import com.yuhyeon.devwebide.project.domain.ProjectVisibility;
import com.yuhyeon.devwebide.runtime.domain.RuntimeLanguage;

import java.time.LocalDateTime;

/**
 * 프로젝트 생성 응답 DTO
 *
 * 프로젝트 생성 완료 후 FE에서 프로젝트 상세 화면 또는
 * 워크스페이스 화면으로 이동하는 데 필요한 정보를 반환합니다.
 */
public record ProjectCreateResponse(

        /**
         * 생성된 프로젝트 ID
         */
        Long id,

        /**
         * 프로젝트 이름
         */
        String name,

        /**
         * 프로젝트 설명
         */
        String description,

        /**
         * 프로젝트 타입
         */
        ProjectType projectType,

        /**
         * 프로젝트 공개 범위
         */
        ProjectVisibility visibility,

        /**
         * 프로젝트 상태
         */
        ProjectStatus status,

        /**
         * 선택한 런타임 ID
         */
        Long runtimeId,

        /**
         * 런타임 내부 이름
         *
         * 예: node-20, python-3.12
         */
        String runtimeName,

        /**
         * 런타임 화면 표시 이름
         *
         * 예: Node.js 20, Python 3.12
         */
        String runtimeDisplayName,

        /**
         * 런타임 언어 타입
         */
        RuntimeLanguage runtimeLanguage,

        /**
         * 프로젝트 생성 일시
         */
        LocalDateTime createdAt
) {

    /**
     * Project 엔티티를 프로젝트 생성 응답 DTO로 변환합니다.
     *
     * @param project 생성된 프로젝트
     * @return 프로젝트 생성 응답 DTO
     */
    public static ProjectCreateResponse from(Project project) {
        return new ProjectCreateResponse(
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
                project.getCreatedAt()
        );
    }
}