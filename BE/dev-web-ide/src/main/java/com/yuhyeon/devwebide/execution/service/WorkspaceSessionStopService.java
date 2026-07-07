package com.yuhyeon.devwebide.execution.service;

import com.yuhyeon.devwebide.execution.domain.ContainerInstance;
import com.yuhyeon.devwebide.execution.domain.ContainerInstanceStatus;
import com.yuhyeon.devwebide.execution.dto.WorkspaceSessionStopResponse;
import com.yuhyeon.devwebide.execution.repository.ContainerInstanceRepository;
import com.yuhyeon.devwebide.workspace.domain.WorkspaceSession;
import com.yuhyeon.devwebide.workspace.repository.WorkspaceSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkspaceSessionStopService {

    private final WorkspaceSessionRepository workspaceSessionRepository;
    private final ContainerInstanceRepository containerInstanceRepository;
    private final ContainerExecutionService containerExecutionService;

    @Transactional
    public WorkspaceSessionStopResponse stopWorkspaceSession(Long workspaceSessionId) {
        WorkspaceSession workspaceSession = workspaceSessionRepository.findById(workspaceSessionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "워크스페이스 세션을 찾을 수 없습니다."
                ));

        if (!workspaceSession.isRunning()) {
            throw new IllegalArgumentException(
                    "실행 중인 워크스페이스 세션만 중지할 수 있습니다."
            );
        }

        ContainerInstance containerInstance = containerInstanceRepository
                .findTopByWorkspaceSessionIdAndStatusOrderByCreatedAtDesc(
                        workspaceSessionId,
                        ContainerInstanceStatus.RUNNING
                )
                .orElseThrow(() -> new IllegalArgumentException(
                        "실행 중인 컨테이너를 찾을 수 없습니다."
                ));

        containerExecutionService.stop(containerInstance);

        LocalDateTime stoppedAt = LocalDateTime.now();
        containerInstance.stop(stoppedAt);
        workspaceSession.stop(stoppedAt);

        return WorkspaceSessionStopResponse.from(workspaceSession, containerInstance);
    }
}
