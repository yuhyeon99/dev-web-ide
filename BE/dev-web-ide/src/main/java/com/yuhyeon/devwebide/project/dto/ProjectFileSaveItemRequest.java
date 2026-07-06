package com.yuhyeon.devwebide.project.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 파일 저장 요청 항목 DTO
 *
 * 열린 dirty 파일 중 저장할 파일 하나의 정보를 담습니다.
 */
public record ProjectFileSaveItemRequest(

        /**
         * 저장 대상 프로젝트 파일 ID
         */
        @NotNull(message = "projectFileId는 필수입니다.")
        Long projectFileId,

        /**
         * 저장할 파일 내용
         *
         * 빈 파일도 저장할 수 있으므로 @NotBlank가 아니라 @NotNull만 사용합니다.
         */
        @NotNull(message = "content는 필수입니다.")
        String content
) {
}