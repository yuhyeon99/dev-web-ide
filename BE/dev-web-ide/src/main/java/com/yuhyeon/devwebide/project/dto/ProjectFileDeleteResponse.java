package com.yuhyeon.devwebide.project.dto;

import com.yuhyeon.devwebide.project.domain.ProjectFile;
import com.yuhyeon.devwebide.project.domain.ProjectFileStatus;
import com.yuhyeon.devwebide.project.domain.ProjectFileType;

import java.time.LocalDateTime;

public record ProjectFileDeleteResponse(
        Long projectFileId,
        ProjectFileType fileType,
        String path,
        ProjectFileStatus status,
        boolean deleted,
        LocalDateTime updatedAt
) {

    public static ProjectFileDeleteResponse from(ProjectFile projectFile) {
        return new ProjectFileDeleteResponse(
                projectFile.getId(),
                projectFile.getFileType(),
                projectFile.getPath(),
                projectFile.getStatus(),
                projectFile.isDeleted(),
                projectFile.getUpdatedAt()
        );
    }
}
