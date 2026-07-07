package com.yuhyeon.devwebide.project.dto;

import com.yuhyeon.devwebide.project.domain.ProjectFile;

import java.time.LocalDateTime;

public record ProjectFileContentResponse(
        Long projectFileId,
        String name,
        String path,
        String mimeType,
        Long sizeBytes,
        String content,
        LocalDateTime updatedAt
) {

    public static ProjectFileContentResponse of(
            ProjectFile projectFile,
            String content
    ) {
        return new ProjectFileContentResponse(
                projectFile.getId(),
                projectFile.getName(),
                projectFile.getPath(),
                projectFile.getMimeType(),
                projectFile.getSizeBytes(),
                content,
                projectFile.getUpdatedAt()
        );
    }
}
