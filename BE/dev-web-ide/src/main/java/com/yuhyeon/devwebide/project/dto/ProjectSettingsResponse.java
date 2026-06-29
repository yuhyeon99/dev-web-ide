package com.yuhyeon.devwebide.project.dto;

import com.yuhyeon.devwebide.project.domain.ProjectSettings;

/**
 * 프로젝트 설정 응답 DTO
 *
 * 프로젝트 상세 조회 시 IDE 설정 정보를 반환합니다.
 */
public record ProjectSettingsResponse(
        boolean autoSaveEnabled,
        boolean formatOnSaveEnabled,
        boolean guestCanEdit,
        boolean shareCursorPosition
) {

    /**
     * ProjectSettings 엔티티를 응답 DTO로 변환합니다.
     *
     * @param projectSettings 프로젝트 설정 엔티티
     * @return 프로젝트 설정 응답 DTO
     */
    public static ProjectSettingsResponse from(ProjectSettings projectSettings) {
        return new ProjectSettingsResponse(
                projectSettings.isAutoSaveEnabled(),
                projectSettings.isFormatOnSaveEnabled(),
                projectSettings.isGuestCanEdit(),
                projectSettings.isShareCursorPosition()
        );
    }
}