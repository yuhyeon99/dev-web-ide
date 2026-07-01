package com.yuhyeon.devwebide.project.dto;

import com.yuhyeon.devwebide.project.domain.ProjectFile;
import com.yuhyeon.devwebide.project.domain.ProjectFileStatus;
import com.yuhyeon.devwebide.project.domain.ProjectFileType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 프로젝트 파일 트리 조회 응답 DTO
 *
 * 웹 IDE 좌측 파일 탐색기에 표시할 파일/폴더 정보를 반환합니다.
 * 실제 파일 원본은 EFS에 저장하고,
 * 이 응답은 PROJECT_FILES 테이블의 메타데이터를 기준으로 구성합니다.
 */
public record ProjectFileTreeResponse(
        Long id,
        Long parentFileId,
        String name,
        String path,
        ProjectFileType fileType,
        String mimeType,
        Long sizeBytes,
        ProjectFileStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ProjectFileTreeResponse> children
) {

    /**
     * ProjectFile 엔티티를 파일 트리 응답 DTO로 변환합니다.
     *
     * @param projectFile 파일/폴더 엔티티
     * @param children 하위 파일/폴더 목록
     * @return 파일 트리 응답 DTO
     */
    public static ProjectFileTreeResponse from(
            ProjectFile projectFile,
            List<ProjectFileTreeResponse> children
    ) {
        return new ProjectFileTreeResponse(
                projectFile.getId(),
                getParentFileId(projectFile),
                projectFile.getName(),
                projectFile.getPath(),
                projectFile.getFileType(),
                projectFile.getMimeType(),
                projectFile.getSizeBytes(),
                projectFile.getStatus(),
                projectFile.getCreatedAt(),
                projectFile.getUpdatedAt(),
                children
        );
    }

    /**
     * 자식이 없는 파일/폴더를 응답 DTO로 변환합니다.
     *
     * @param projectFile 파일/폴더 엔티티
     * @return 파일 트리 응답 DTO
     */
    public static ProjectFileTreeResponse leaf(ProjectFile projectFile) {
        return from(projectFile, List.of());
    }

    /**
     * 부모 파일 ID를 반환합니다.
     *
     * 루트 디렉토리는 parentFile이 없으므로 null을 반환합니다.
     *
     * @param projectFile 파일/폴더 엔티티
     * @return 부모 파일 ID
     */
    private static Long getParentFileId(ProjectFile projectFile) {
        if (projectFile.getParentFile() == null) {
            return null;
        }

        return projectFile.getParentFile().getId();
    }
}