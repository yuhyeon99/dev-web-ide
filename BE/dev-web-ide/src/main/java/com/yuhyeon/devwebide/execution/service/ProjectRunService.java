package com.yuhyeon.devwebide.execution.service;

import com.yuhyeon.devwebide.execution.domain.ContainerInstance;
import com.yuhyeon.devwebide.execution.dto.ProjectRunRequest;
import com.yuhyeon.devwebide.execution.dto.ProjectRunResponse;
import com.yuhyeon.devwebide.execution.repository.ContainerInstanceRepository;
import com.yuhyeon.devwebide.project.domain.Project;
import com.yuhyeon.devwebide.project.domain.ProjectStatus;
import com.yuhyeon.devwebide.project.repository.ProjectRepository;
import com.yuhyeon.devwebide.runtime.domain.Runtime;
import com.yuhyeon.devwebide.user.domain.GuestSession;
import com.yuhyeon.devwebide.user.domain.User;
import com.yuhyeon.devwebide.user.repository.GuestSessionRepository;
import com.yuhyeon.devwebide.user.repository.UserRepository;
import com.yuhyeon.devwebide.workspace.domain.WorkspaceSession;
import com.yuhyeon.devwebide.workspace.domain.WorkspaceSessionStatus;
import com.yuhyeon.devwebide.workspace.repository.WorkspaceSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 프로젝트 실행 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectRunService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final GuestSessionRepository guestSessionRepository;
    private final WorkspaceSessionRepository workspaceSessionRepository;
    private final ContainerInstanceRepository containerInstanceRepository;
    private final ContainerExecutionService containerExecutionService;

    @Transactional
    public ProjectRunResponse runProject(
            Long projectId,
            ProjectRunRequest request
    ) {
        validateRunRequester(request);

        Project project = projectRepository.findByIdAndStatus(
                        projectId,
                        ProjectStatus.ACTIVE
                )
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));

        User user = null;
        GuestSession guestSession = null;

        if (request.isUserRequest()) {
            user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        }

        if (request.isGuestRequest()) {
            guestSession = guestSessionRepository.findById(request.guestSessionId())
                    .orElseThrow(() -> new IllegalArgumentException("게스트 세션을 찾을 수 없습니다."));
        }

        Runtime runtime = project.getRuntime();

        WorkspaceSession workspaceSession = WorkspaceSession.builder()
                .project(project)
                .runtime(runtime)
                .user(user)
                .guestSession(guestSession)
                .status(WorkspaceSessionStatus.STARTING)
                .build();

        WorkspaceSession savedWorkspaceSession =
                workspaceSessionRepository.save(workspaceSession);

        ContainerStartResult startResult = containerExecutionService.start(
                project,
                runtime,
                savedWorkspaceSession
        );

        ContainerInstance containerInstance = ContainerInstance.builder()
                .workspaceSession(savedWorkspaceSession)
                .provider(startResult.provider())
                .taskArn(startResult.taskArn())
                .containerId(startResult.containerId())
                .dockerImage(startResult.dockerImage())
                .status(startResult.status())
                .efsMountPath(startResult.efsMountPath())
                .build();

        ContainerInstance savedContainerInstance =
                containerInstanceRepository.save(containerInstance);

        return ProjectRunResponse.from(
                savedWorkspaceSession,
                savedContainerInstance
        );
    }

    private void validateRunRequester(ProjectRunRequest request) {
        boolean hasUser = request.userId() != null;
        boolean hasGuestSession = request.guestSessionId() != null;

        if (!hasUser && !hasGuestSession) {
            throw new IllegalArgumentException("회원 또는 게스트 세션 정보가 필요합니다.");
        }

        if (hasUser && hasGuestSession) {
            throw new IllegalArgumentException("회원과 게스트 세션은 동시에 사용할 수 없습니다.");
        }
    }
}