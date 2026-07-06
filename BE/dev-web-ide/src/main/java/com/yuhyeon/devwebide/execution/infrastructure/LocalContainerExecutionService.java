package com.yuhyeon.devwebide.execution.service;

import com.yuhyeon.devwebide.execution.domain.ContainerInstanceStatus;
import com.yuhyeon.devwebide.project.domain.Project;
import com.yuhyeon.devwebide.runtime.domain.Runtime;
import com.yuhyeon.devwebide.workspace.domain.WorkspaceSession;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 로컬 컨테이너 실행 서비스
 *
 * 실제 ECS/Fargate 연동 전까지 사용하는 임시 구현체입니다.
 */
@Service
public class LocalContainerExecutionService implements ContainerExecutionService {

    private static final String PROVIDER = "LOCAL";

    @Override
    public ContainerStartResult start(
            Project project,
            Runtime runtime,
            WorkspaceSession workspaceSession
    ) {
        return new ContainerStartResult(
                PROVIDER,
                null,
                "local-" + UUID.randomUUID(),
                runtime.getDockerImage(),
                ContainerInstanceStatus.RUNNING,
                project.getStoragePath()
        );
    }
}