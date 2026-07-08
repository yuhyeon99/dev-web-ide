package com.yuhyeon.devwebide.project.dto;

import com.yuhyeon.devwebide.project.domain.Project;
import com.yuhyeon.devwebide.project.domain.ProjectStatus;

import java.time.LocalDateTime;

public record ProjectDeleteResponse(
        Long projectId,
        ProjectStatus status,
        boolean deleted,
        LocalDateTime updatedAt
) {

    public static ProjectDeleteResponse from(Project project) {
        return new ProjectDeleteResponse(
                project.getId(),
                project.getStatus(),
                project.getStatus() == ProjectStatus.DELETED,
                project.getUpdatedAt()
        );
    }
}
