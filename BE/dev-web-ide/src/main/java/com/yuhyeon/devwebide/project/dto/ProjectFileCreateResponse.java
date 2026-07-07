package com.yuhyeon.devwebide.project.dto;

import com.yuhyeon.devwebide.project.domain.ProjectFile;
import com.yuhyeon.devwebide.project.domain.ProjectFileStatus;
import com.yuhyeon.devwebide.project.domain.ProjectFileType;

import java.time.LocalDateTime;

public record ProjectFileCreateResponse(
        Long projectFileId,
        Long parentFileId,
        String name,
        String path,
        ProjectFileType fileType,
        String mimeType,
        Long sizeBytes,
        ProjectFileStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static ProjectFileCreateResponse from(ProjectFile projectFile) {
        return new ProjectFileCreateResponse(
                projectFile.getId(),
                getParentFileId(projectFile),
                projectFile.getName(),
                projectFile.getPath(),
                projectFile.getFileType(),
                projectFile.getMimeType(),
                projectFile.getSizeBytes(),
                projectFile.getStatus(),
                projectFile.getCreatedAt(),
                projectFile.getUpdatedAt()
        );
    }

    private static Long getParentFileId(ProjectFile projectFile) {
        if (projectFile.getParentFile() == null) {
            return null;
        }

        return projectFile.getParentFile().getId();
    }
}
