package com.yuhyeon.devwebide.project.dto;

import com.yuhyeon.devwebide.project.domain.ProjectType;
import com.yuhyeon.devwebide.project.domain.ProjectVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 프로젝트 생성 요청 DTO
 *
 * 개인 프로젝트, 팀 프로젝트, 게스트 프로젝트 생성 시 사용합니다.
 *
 * ownerUserId, guestSessionId는 클라이언트 요청값으로 받지 않고,
 * 인증/게스트 세션 정보에서 서버가 판단합니다.
 */
public record ProjectCreateRequest(

        /**
         * 프로젝트 이름
         */
        @NotBlank(message = "프로젝트 이름은 필수입니다.")
        @Size(max = 200, message = "프로젝트 이름은 200자 이하여야 합니다.")
        String name,

        /**
         * 프로젝트 설명
         */
        @Size(max = 500, message = "프로젝트 설명은 500자 이하여야 합니다.")
        String description,

        /**
         * 선택한 런타임 ID
         *
         * GET /api/runtimes 응답에서 선택한 Runtime ID입니다.
         */
        @NotNull(message = "런타임 ID는 필수입니다.")
        Long runtimeId,

        /**
         * 프로젝트 타입
         *
         * PERSONAL, TEAM, GUEST
         */
        @NotNull(message = "프로젝트 타입은 필수입니다.")
        ProjectType projectType,

        /**
         * 프로젝트 공개 범위
         *
         * PRIVATE, TEAM
         */
        @NotNull(message = "프로젝트 공개 범위는 필수입니다.")
        ProjectVisibility visibility,

        /**
         * 팀 프로젝트 생성 시 초대할 사용자 ID 목록
         *
         * 개인 프로젝트와 게스트 프로젝트에서는 빈 리스트로 처리합니다.
         */
        List<Long> memberUserIds
) {

    /**
     * record의 축약 생성자
     * memberUserIds가 null인 경우 빈 리스트로 보정합니다.
     */
    public ProjectCreateRequest {
        if (memberUserIds == null) {
            memberUserIds = List.of();
        }
    }

    /**
     * 팀 프로젝트 생성 요청 여부 확인
     *
     * @return 팀 프로젝트 여부
     */
    public boolean isTeamProject() {
        return projectType == ProjectType.TEAM;
    }

    /**
     * 게스트 프로젝트 생성 요청 여부 확인
     *
     * @return 게스트 프로젝트 여부
     */
    public boolean isGuestProject() {
        return projectType == ProjectType.GUEST;
    }

    /**
     * 개인 프로젝트 생성 요청 여부 확인
     *
     * @return 개인 프로젝트 여부
     */
    public boolean isPersonalProject() {
        return projectType == ProjectType.PERSONAL;
    }
}