package com.yuhyeon.devwebide.execution.repository;

import com.yuhyeon.devwebide.execution.domain.ContainerInstance;
import com.yuhyeon.devwebide.execution.domain.ContainerInstanceStatus;
import com.yuhyeon.devwebide.project.domain.Project;
import com.yuhyeon.devwebide.project.domain.ProjectStatus;
import com.yuhyeon.devwebide.project.domain.ProjectType;
import com.yuhyeon.devwebide.project.domain.ProjectVisibility;
import com.yuhyeon.devwebide.project.repository.ProjectRepository;
import com.yuhyeon.devwebide.runtime.domain.Runtime;
import com.yuhyeon.devwebide.runtime.domain.RuntimeLanguage;
import com.yuhyeon.devwebide.runtime.domain.RuntimeStatus;
import com.yuhyeon.devwebide.runtime.repository.RuntimeRepository;
import com.yuhyeon.devwebide.user.domain.User;
import com.yuhyeon.devwebide.user.domain.UserRole;
import com.yuhyeon.devwebide.user.domain.UserStatus;
import com.yuhyeon.devwebide.user.repository.UserRepository;
import com.yuhyeon.devwebide.workspace.domain.WorkspaceSession;
import com.yuhyeon.devwebide.workspace.domain.WorkspaceSessionStatus;
import com.yuhyeon.devwebide.workspace.repository.WorkspaceSessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ContainerInstanceRepositoryTest {

    @Autowired
    private ContainerInstanceRepository containerInstanceRepository;

    @Autowired
    private WorkspaceSessionRepository workspaceSessionRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private RuntimeRepository runtimeRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("컨테이너 인스턴스를 저장한다")
    void saveContainerInstance() {
        // given
        WorkspaceSession workspaceSession = createWorkspaceSession();

        ContainerInstance containerInstance = ContainerInstance.builder()
                .workspaceSession(workspaceSession)
                .provider("LOCAL")
                .taskArn(null)
                .containerId("container-001")
                .dockerImage("node:20")
                .status(ContainerInstanceStatus.STARTING)
                .efsMountPath("/workspace")
                .build();

        // when
        ContainerInstance savedContainerInstance =
                containerInstanceRepository.save(containerInstance);

        // then
        assertThat(savedContainerInstance.getId()).isNotNull();
        assertThat(savedContainerInstance.getWorkspaceSession().getId())
                .isEqualTo(workspaceSession.getId());
        assertThat(savedContainerInstance.getProvider()).isEqualTo("LOCAL");
        assertThat(savedContainerInstance.getContainerId()).isEqualTo("container-001");
        assertThat(savedContainerInstance.getDockerImage()).isEqualTo("node:20");
        assertThat(savedContainerInstance.getStatus())
                .isEqualTo(ContainerInstanceStatus.STARTING);
        assertThat(savedContainerInstance.getEfsMountPath()).isEqualTo("/workspace");
    }

    @Test
    @DisplayName("워크스페이스 세션 ID 기준으로 컨테이너 인스턴스 목록을 조회한다")
    void findByWorkspaceSessionIdOrderByCreatedAtDesc() {
        // given
        WorkspaceSession workspaceSession = createWorkspaceSession();

        ContainerInstance firstContainer = createContainerInstance(
                workspaceSession,
                "container-001",
                "task-001",
                ContainerInstanceStatus.STOPPED
        );

        ContainerInstance secondContainer = createContainerInstance(
                workspaceSession,
                "container-002",
                "task-002",
                ContainerInstanceStatus.RUNNING
        );

        containerInstanceRepository.save(firstContainer);
        containerInstanceRepository.save(secondContainer);

        // when
        List<ContainerInstance> result =
                containerInstanceRepository.findByWorkspaceSessionIdOrderByCreatedAtDesc(
                        workspaceSession.getId()
                );

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(ContainerInstance::getContainerId)
                .containsExactlyInAnyOrder("container-001", "container-002");
    }

    @Test
    @DisplayName("워크스페이스 세션 ID 기준으로 최신 컨테이너 인스턴스를 조회한다")
    void findTopByWorkspaceSessionIdOrderByCreatedAtDesc() {
        // given
        WorkspaceSession workspaceSession = createWorkspaceSession();

        ContainerInstance containerInstance = createContainerInstance(
                workspaceSession,
                "container-001",
                "task-001",
                ContainerInstanceStatus.RUNNING
        );

        containerInstanceRepository.save(containerInstance);

        // when
        Optional<ContainerInstance> result =
                containerInstanceRepository.findTopByWorkspaceSessionIdOrderByCreatedAtDesc(
                        workspaceSession.getId()
                );

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getContainerId()).isEqualTo("container-001");
        assertThat(result.get().getWorkspaceSession().getId())
                .isEqualTo(workspaceSession.getId());
    }

    @Test
    @DisplayName("컨테이너 상태 기준으로 컨테이너 인스턴스 목록을 조회한다")
    void findByStatusOrderByCreatedAtDesc() {
        // given
        WorkspaceSession workspaceSession = createWorkspaceSession();

        ContainerInstance runningContainer = createContainerInstance(
                workspaceSession,
                "container-running",
                "task-running",
                ContainerInstanceStatus.RUNNING
        );

        ContainerInstance stoppedContainer = createContainerInstance(
                workspaceSession,
                "container-stopped",
                "task-stopped",
                ContainerInstanceStatus.STOPPED
        );

        containerInstanceRepository.save(runningContainer);
        containerInstanceRepository.save(stoppedContainer);

        // when
        List<ContainerInstance> result =
                containerInstanceRepository.findByStatusOrderByCreatedAtDesc(
                        ContainerInstanceStatus.RUNNING
                );

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContainerId()).isEqualTo("container-running");
        assertThat(result.get(0).getStatus()).isEqualTo(ContainerInstanceStatus.RUNNING);
    }

    @Test
    @DisplayName("워크스페이스 세션 ID와 상태 기준으로 최신 컨테이너 인스턴스를 조회한다")
    void findTopByWorkspaceSessionIdAndStatusOrderByCreatedAtDesc() {
        // given
        WorkspaceSession workspaceSession = createWorkspaceSession();

        ContainerInstance stoppedContainer = createContainerInstance(
                workspaceSession,
                "container-stopped",
                "task-stopped",
                ContainerInstanceStatus.STOPPED
        );

        ContainerInstance runningContainer = createContainerInstance(
                workspaceSession,
                "container-running",
                "task-running",
                ContainerInstanceStatus.RUNNING
        );

        containerInstanceRepository.save(stoppedContainer);
        containerInstanceRepository.save(runningContainer);

        // when
        Optional<ContainerInstance> result =
                containerInstanceRepository.findTopByWorkspaceSessionIdAndStatusOrderByCreatedAtDesc(
                        workspaceSession.getId(),
                        ContainerInstanceStatus.RUNNING
                );

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getContainerId()).isEqualTo("container-running");
        assertThat(result.get().getStatus()).isEqualTo(ContainerInstanceStatus.RUNNING);
    }

    @Test
    @DisplayName("워크스페이스 세션 ID와 상태 기준으로 컨테이너 존재 여부를 확인한다")
    void existsByWorkspaceSessionIdAndStatus() {
        // given
        WorkspaceSession workspaceSession = createWorkspaceSession();

        ContainerInstance runningContainer = createContainerInstance(
                workspaceSession,
                "container-running",
                "task-running",
                ContainerInstanceStatus.RUNNING
        );

        containerInstanceRepository.save(runningContainer);

        // when
        boolean existsRunningContainer =
                containerInstanceRepository.existsByWorkspaceSessionIdAndStatus(
                        workspaceSession.getId(),
                        ContainerInstanceStatus.RUNNING
                );

        boolean existsFailedContainer =
                containerInstanceRepository.existsByWorkspaceSessionIdAndStatus(
                        workspaceSession.getId(),
                        ContainerInstanceStatus.FAILED
                );

        // then
        assertThat(existsRunningContainer).isTrue();
        assertThat(existsFailedContainer).isFalse();
    }

    @Test
    @DisplayName("ECS Task ARN 기준으로 컨테이너 인스턴스를 조회한다")
    void findByTaskArn() {
        // given
        WorkspaceSession workspaceSession = createWorkspaceSession();

        ContainerInstance containerInstance = createContainerInstance(
                workspaceSession,
                "container-001",
                "arn:aws:ecs:ap-northeast-2:123456789012:task/devwebide/task-001",
                ContainerInstanceStatus.RUNNING
        );

        containerInstanceRepository.save(containerInstance);

        // when
        Optional<ContainerInstance> result =
                containerInstanceRepository.findByTaskArn(
                        "arn:aws:ecs:ap-northeast-2:123456789012:task/devwebide/task-001"
                );

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getContainerId()).isEqualTo("container-001");
        assertThat(result.get().getStatus()).isEqualTo(ContainerInstanceStatus.RUNNING);
    }

    @Test
    @DisplayName("컨테이너 ID 기준으로 컨테이너 인스턴스를 조회한다")
    void findByContainerId() {
        // given
        WorkspaceSession workspaceSession = createWorkspaceSession();

        ContainerInstance containerInstance = createContainerInstance(
                workspaceSession,
                "container-001",
                "task-001",
                ContainerInstanceStatus.RUNNING
        );

        containerInstanceRepository.save(containerInstance);

        // when
        Optional<ContainerInstance> result =
                containerInstanceRepository.findByContainerId("container-001");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getTaskArn()).isEqualTo("task-001");
        assertThat(result.get().getDockerImage()).isEqualTo("node:20");
    }

    @Test
    @DisplayName("컨테이너 실행 상태를 RUNNING으로 변경한다")
    void markRunning() {
        // given
        WorkspaceSession workspaceSession = createWorkspaceSession();

        ContainerInstance containerInstance = ContainerInstance.builder()
                .workspaceSession(workspaceSession)
                .provider("ECS")
                .taskArn(null)
                .containerId(null)
                .dockerImage("node:20")
                .status(ContainerInstanceStatus.STARTING)
                .efsMountPath("/workspace")
                .build();

        ContainerInstance savedContainerInstance =
                containerInstanceRepository.save(containerInstance);

        // when
        savedContainerInstance.markRunning("task-running", "container-running");

        // then
        assertThat(savedContainerInstance.getStatus())
                .isEqualTo(ContainerInstanceStatus.RUNNING);
        assertThat(savedContainerInstance.getTaskArn()).isEqualTo("task-running");
        assertThat(savedContainerInstance.getContainerId()).isEqualTo("container-running");
        assertThat(savedContainerInstance.isRunning()).isTrue();
    }

    @Test
    @DisplayName("컨테이너 종료 처리 시 상태와 종료 시간이 저장된다")
    void stopContainer() {
        // given
        WorkspaceSession workspaceSession = createWorkspaceSession();

        ContainerInstance containerInstance = createContainerInstance(
                workspaceSession,
                "container-001",
                "task-001",
                ContainerInstanceStatus.RUNNING
        );

        ContainerInstance savedContainerInstance =
                containerInstanceRepository.save(containerInstance);

        // when
        savedContainerInstance.stop(java.time.LocalDateTime.of(2026, 7, 6, 12, 0));

        // then
        assertThat(savedContainerInstance.getStatus())
                .isEqualTo(ContainerInstanceStatus.STOPPED);
        assertThat(savedContainerInstance.getStoppedAt()).isNotNull();
        assertThat(savedContainerInstance.isStopped()).isTrue();
    }

    @Test
    @DisplayName("컨테이너 실패 처리 시 상태와 종료 시간이 저장된다")
    void failContainer() {
        // given
        WorkspaceSession workspaceSession = createWorkspaceSession();

        ContainerInstance containerInstance = createContainerInstance(
                workspaceSession,
                "container-001",
                "task-001",
                ContainerInstanceStatus.STARTING
        );

        ContainerInstance savedContainerInstance =
                containerInstanceRepository.save(containerInstance);

        // when
        savedContainerInstance.fail(java.time.LocalDateTime.of(2026, 7, 6, 12, 30));

        // then
        assertThat(savedContainerInstance.getStatus())
                .isEqualTo(ContainerInstanceStatus.FAILED);
        assertThat(savedContainerInstance.getStoppedAt()).isNotNull();
        assertThat(savedContainerInstance.isFailed()).isTrue();
    }

    private ContainerInstance createContainerInstance(
            WorkspaceSession workspaceSession,
            String containerId,
            String taskArn,
            ContainerInstanceStatus status
    ) {
        return ContainerInstance.builder()
                .workspaceSession(workspaceSession)
                .provider("ECS")
                .taskArn(taskArn)
                .containerId(containerId)
                .dockerImage("node:20")
                .status(status)
                .efsMountPath("/workspace")
                .build();
    }

    private WorkspaceSession createWorkspaceSession() {
        User user = userRepository.save(
                User.builder()
                        .email("test@example.com")
                        .nickname("테스트사용자")
                        .role(UserRole.USER)
                        .status(UserStatus.ACTIVE)
                        .build()
        );

        Runtime runtime = runtimeRepository.save(
                Runtime.builder()
                        .name("node")
                        .displayName("Node.js")
                        .version("20")
                        .dockerImage("node:20")
                        .language(RuntimeLanguage.NODE)
                        .status(RuntimeStatus.ACTIVE)
                        .build()
        );

        Project project = projectRepository.save(
                Project.builder()
                        .ownerUser(user)
                        .guestSession(null)
                        .runtime(runtime)
                        .name("테스트 프로젝트")
                        .description("테스트 프로젝트입니다.")
                        .projectType(ProjectType.PERSONAL)
                        .visibility(ProjectVisibility.PRIVATE)
                        .status(ProjectStatus.ACTIVE)
                        .storagePath("/projects/test-project")
                        .build()
        );

        WorkspaceSession workspaceSession = WorkspaceSession.builder()
                .project(project)
                .runtime(runtime)
                .user(user)
                .guestSession(null)
                .status(WorkspaceSessionStatus.RUNNING)
                .build();

        return workspaceSessionRepository.save(workspaceSession);
    }
}