package com.yuhyeon.devwebide.project.dto;

import com.yuhyeon.devwebide.project.domain.FileVersion;

/**
 * 저장된 파일 응답 DTO
 *
 * 파일 저장 후 생성된 파일 버전 정보를 반환합니다.
 */
public record SavedFileResponse(

        /**
         * 저장된 프로젝트 파일 ID
         */
        Long projectFileId,

        /**
         * 프로젝트 내부 파일 경로
         */
        String path,

        /**
         * 생성된 파일 버전 번호
         */
        Integer versionNo,

        /**
         * 저장된 파일 크기
         */
        Long sizeBytes,

        /**
         * 저장된 파일 내용 해시
         */
        String contentHash
) {

    /**
     * FileVersion 엔티티를 응답 DTO로 변환합니다.
     *
     * @param fileVersion 파일 버전 엔티티
     * @return 저장된 파일 응답 DTO
     */
    public static SavedFileResponse from(FileVersion fileVersion) {
        return new SavedFileResponse(
                fileVersion.getProjectFile().getId(),
                fileVersion.getProjectFile().getPath(),
                fileVersion.getVersionNo(),
                fileVersion.getSizeBytes(),
                fileVersion.getContentHash()
        );
    }
}