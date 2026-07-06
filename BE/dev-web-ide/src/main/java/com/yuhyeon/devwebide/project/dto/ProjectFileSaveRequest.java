package com.yuhyeon.devwebide.project.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 프로젝트 파일 일괄 저장 요청 DTO
 *
 * 열린 dirty 파일들을 한 번에 저장합니다.
 * 초기 인증 연동 전이므로 userId 또는 guestSessionId를 요청으로 받습니다.
 */
public record ProjectFileSaveRequest(

        /**
         * 저장 요청 사용자 ID
         *
         * 회원 저장 요청인 경우 사용합니다.
         */
        Long userId,

        /**
         * 저장 요청 게스트 세션 ID
         *
         * 게스트 저장 요청인 경우 사용합니다.
         */
        Long guestSessionId,

        /**
         * 저장할 파일 목록
         */
        @Valid
        @NotEmpty(message = "저장할 파일 목록은 필수입니다.")
        List<ProjectFileSaveItemRequest> files
) {
}