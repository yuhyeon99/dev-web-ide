package com.yuhyeon.devwebide.project.dto;

import com.yuhyeon.devwebide.project.domain.ProjectSaveBatchStatus;

import java.util.List;

/**
 * 프로젝트 파일 일괄 저장 응답 DTO
 *
 * 저장 배치 결과와 파일별 저장 결과를 반환합니다.
 */
public record ProjectFileSaveResponse(

        /**
         * 프로젝트 ID
         */
        Long projectId,

        /**
         * 저장 배치 ID
         */
        Long saveBatchId,

        /**
         * 저장 배치 상태
         */
        ProjectSaveBatchStatus status,

        /**
         * 저장된 파일 수
         */
        Integer savedFileCount,

        /**
         * 저장된 파일 목록
         */
        List<SavedFileResponse> files
) {

    /**
     * 프로젝트 파일 저장 응답을 생성합니다.
     *
     * @param projectId 프로젝트 ID
     * @param saveBatchId 저장 배치 ID
     * @param status 저장 상태
     * @param savedFileCount 저장된 파일 수
     * @param files 저장된 파일 목록
     * @return 파일 저장 응답 DTO
     */
    public static ProjectFileSaveResponse of(
            Long projectId,
            Long saveBatchId,
            ProjectSaveBatchStatus status,
            Integer savedFileCount,
            List<SavedFileResponse> files
    ) {
        return new ProjectFileSaveResponse(
                projectId,
                saveBatchId,
                status,
                savedFileCount,
                files
        );
    }
}