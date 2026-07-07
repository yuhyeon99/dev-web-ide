package com.yuhyeon.devwebide.execution.dto;

import com.yuhyeon.devwebide.execution.domain.ContainerInstance;
import com.yuhyeon.devwebide.execution.domain.ContainerInstanceStatus;
import com.yuhyeon.devwebide.workspace.domain.WorkspaceSession;
import com.yuhyeon.devwebide.workspace.domain.WorkspaceSessionStatus;

import java.time.LocalDateTime;

public record WorkspaceSessionStopResponse(
        Long workspaceSessionId,
        Long containerInstanceId,
        WorkspaceSessionStatus workspaceStatus,
        ContainerInstanceStatus containerStatus,
        LocalDateTime stoppedAt
) {

    public static WorkspaceSessionStopResponse from(
            WorkspaceSession workspaceSession,
            ContainerInstance containerInstance
    ) {
        return new WorkspaceSessionStopResponse(
                workspaceSession.getId(),
                containerInstance.getId(),
                workspaceSession.getStatus(),
                containerInstance.getStatus(),
                workspaceSession.getStoppedAt()
        );
    }
}
