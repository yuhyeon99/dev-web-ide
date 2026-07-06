package com.yuhyeon.devwebide.execution.service;

import com.yuhyeon.devwebide.execution.domain.ContainerInstance;
import com.yuhyeon.devwebide.execution.domain.ContainerInstanceStatus;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ProjectRunServiceTest {

    private static final Long PROJECT_ID = 1L;
    private static final Long USER_ID = 10L;
    private static final Long GUEST_SESSION_ID = 20L;
    private static final Long WORKSPACE_SESSION_ID = 100L;
    private static final Long CONTAINER_INSTANCE_ID = 200L;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GuestSessionRepository guestSessionRepository;

    @Mock
    private WorkspaceSessionRepository workspaceSessionRepository;

    @Mock
    private ContainerInstanceRepository containerInstanceRepository;

    @Mock
    private ContainerExecutionService containerExecutionService;

    @InjectMocks
    private ProjectRunService projectRunService;

    private Project project;
    private Runtime runtime;
    private User user;
    private GuestSession guestSession;
    private ContainerStartResult containerStartResult;

    @Test
    @DisplayName("회원 프로젝트 실행에 성공한다")
    void runProjectByUser() {
        // given
        ProjectRunRequest request = new ProjectRunRequest(USER_ID, null);

        givenActiveProject();

        given(userRepository.findById(USER_ID))
                .willReturn(Optional.of(user));

        givenWorkspaceSessionSaved();
        givenContainerStarted();
        givenContainerInstanceSaved();

        // when
        ProjectRunResponse response = projectRunService.runProject(PROJECT_ID, request);

        // then
        assertThat(response.projectId()).isEqualTo(PROJECT_ID);
        assertThat(response.workspaceSessionId()).isEqualTo(WORKSPACE_SESSION_ID);
        assertThat(response.containerInstanceId()).isEqualTo(CONTAINER_INSTANCE_ID);
        assertThat(response.runtimeName()).isEqualTo("nodejs");
        assertThat(response.runtimeDisplayName()).isEqualTo("Node.js");
        assertThat(response.dockerImage()).isEqualTo("node:20");
        assertThat(response.workspaceStatus()).isEqualTo(WorkspaceSessionStatus.STARTING);
        assertThat(response.efsMountPath()).isEqualTo("/projects/1");

        then(projectRepository).should()
                .findByIdAndStatus(PROJECT_ID, ProjectStatus.ACTIVE);

        then(userRepository).should()
                .findById(USER_ID);

        then(guestSessionRepository).should(never())
                .findById(any());

        then(containerExecutionService).should()
                .start(eq(project), eq(runtime), any(WorkspaceSession.class));
    }

    @BeforeEach
    void setUp() {
        project = mock(Project.class);
        runtime = mock(Runtime.class);
        user = mock(User.class);
        guestSession = mock(GuestSession.class);

        containerStartResult = new ContainerStartResult(
                "LOCAL",
                null,
                "local-container-1",
                "node:20",
                ContainerInstanceStatus.RUNNING,
                "/projects/1"
        );
    }

    @Test
    @DisplayName("게스트 프로젝트 실행에 성공한다")
    void runProjectByGuest() {
        // given
        ProjectRunRequest request = new ProjectRunRequest(null, GUEST_SESSION_ID);

        givenActiveProject();

        given(guestSessionRepository.findById(GUEST_SESSION_ID))
                .willReturn(Optional.of(guestSession));

        givenWorkspaceSessionSaved();
        givenContainerStarted();
        givenContainerInstanceSaved();

        // when
        ProjectRunResponse response = projectRunService.runProject(PROJECT_ID, request);

        // then
        assertThat(response.projectId()).isEqualTo(PROJECT_ID);
        assertThat(response.workspaceSessionId()).isEqualTo(WORKSPACE_SESSION_ID);
        assertThat(response.containerInstanceId()).isEqualTo(CONTAINER_INSTANCE_ID);
        assertThat(response.container().provider()).isEqualTo("LOCAL");
        assertThat(response.container().containerId()).isEqualTo("local-container-1");
        assertThat(response.container().status()).isEqualTo(ContainerInstanceStatus.RUNNING);

        then(userRepository).should(never())
                .findById(any());

        then(guestSessionRepository).should()
                .findById(GUEST_SESSION_ID);
    }

    @Test
    @DisplayName("프로젝트 실행 시 WorkspaceSession을 저장한다")
    void saveWorkspaceSession() {
        // given
        ProjectRunRequest request = new ProjectRunRequest(USER_ID, null);

        givenActiveProject();

        given(userRepository.findById(USER_ID))
                .willReturn(Optional.of(user));

        givenWorkspaceSessionSaved();
        givenContainerStarted();
        givenContainerInstanceSaved();

        // when
        projectRunService.runProject(PROJECT_ID, request);

        // then
        ArgumentCaptor<WorkspaceSession> captor =
                ArgumentCaptor.forClass(WorkspaceSession.class);

        then(workspaceSessionRepository).should()
                .save(captor.capture());

        WorkspaceSession savedWorkspaceSession = captor.getValue();

        assertThat(savedWorkspaceSession.getProject()).isSameAs(project);
        assertThat(savedWorkspaceSession.getRuntime()).isSameAs(runtime);
        assertThat(savedWorkspaceSession.getUser()).isSameAs(user);
        assertThat(savedWorkspaceSession.getGuestSession()).isNull();
        assertThat(savedWorkspaceSession.getStatus()).isEqualTo(WorkspaceSessionStatus.STARTING);
        assertThat(savedWorkspaceSession.getStartedAt()).isNotNull();
    }

    @Test
    @DisplayName("프로젝트 실행 시 ContainerInstance를 저장한다")
    void saveContainerInstance() {
        // given
        ProjectRunRequest request = new ProjectRunRequest(USER_ID, null);

        givenActiveProject();

        given(userRepository.findById(USER_ID))
                .willReturn(Optional.of(user));

        givenWorkspaceSessionSaved();
        givenContainerStarted();
        givenContainerInstanceSaved();

        // when
        projectRunService.runProject(PROJECT_ID, request);

        // then
        ArgumentCaptor<ContainerInstance> captor =
                ArgumentCaptor.forClass(ContainerInstance.class);

        then(containerInstanceRepository).should()
                .save(captor.capture());

        ContainerInstance savedContainerInstance = captor.getValue();

        assertThat(savedContainerInstance.getWorkspaceSession().getId())
                .isEqualTo(WORKSPACE_SESSION_ID);

        assertThat(savedContainerInstance.getProvider()).isEqualTo("LOCAL");
        assertThat(savedContainerInstance.getTaskArn()).isNull();
        assertThat(savedContainerInstance.getContainerId()).isEqualTo("local-container-1");
        assertThat(savedContainerInstance.getDockerImage()).isEqualTo("node:20");
        assertThat(savedContainerInstance.getStatus()).isEqualTo(ContainerInstanceStatus.RUNNING);
        assertThat(savedContainerInstance.getEfsMountPath()).isEqualTo("/projects/1");
    }

    @Test
    @DisplayName("컨테이너 실행 서비스에 프로젝트, 런타임, 워크스페이스 세션을 전달한다")
    void callContainerExecutionService() {
        // given
        ProjectRunRequest request = new ProjectRunRequest(USER_ID, null);

        givenActiveProject();

        given(userRepository.findById(USER_ID))
                .willReturn(Optional.of(user));

        givenWorkspaceSessionSaved();
        givenContainerStarted();
        givenContainerInstanceSaved();

        // when
        projectRunService.runProject(PROJECT_ID, request);

        // then
        then(containerExecutionService).should()
                .start(eq(project), eq(runtime), any(WorkspaceSession.class));
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트면 예외가 발생한다")
    void throwExceptionWhenProjectNotFound() {
        // given
        ProjectRunRequest request = new ProjectRunRequest(USER_ID, null);

        given(projectRepository.findByIdAndStatus(PROJECT_ID, ProjectStatus.ACTIVE))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> projectRunService.runProject(PROJECT_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("프로젝트를 찾을 수 없습니다.");

        then(containerExecutionService).should(never())
                .start(any(), any(), any());
    }

    @Test
    @DisplayName("회원 ID와 게스트 세션 ID가 모두 없으면 예외가 발생한다")
    void throwExceptionWhenRequesterIsEmpty() {
        // given
        ProjectRunRequest request = new ProjectRunRequest(null, null);

        // when & then
        assertThatThrownBy(() -> projectRunService.runProject(PROJECT_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 또는 게스트 세션 정보가 필요합니다.");

        then(projectRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("회원 ID와 게스트 세션 ID가 모두 있으면 예외가 발생한다")
    void throwExceptionWhenRequesterIsDuplicated() {
        // given
        ProjectRunRequest request = new ProjectRunRequest(USER_ID, GUEST_SESSION_ID);

        // when & then
        assertThatThrownBy(() -> projectRunService.runProject(PROJECT_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원과 게스트 세션은 동시에 사용할 수 없습니다.");

        then(projectRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("존재하지 않는 회원이면 예외가 발생한다")
    void throwExceptionWhenUserNotFound() {
        // given
        ProjectRunRequest request = new ProjectRunRequest(USER_ID, null);

        given(projectRepository.findByIdAndStatus(PROJECT_ID, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));

        given(userRepository.findById(USER_ID))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> projectRunService.runProject(PROJECT_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");

        then(workspaceSessionRepository).should(never())
                .save(any());

        then(containerExecutionService).should(never())
                .start(any(), any(), any());
    }

    @Test
    @DisplayName("존재하지 않는 게스트 세션이면 예외가 발생한다")
    void throwExceptionWhenGuestSessionNotFound() {
        // given
        ProjectRunRequest request = new ProjectRunRequest(null, GUEST_SESSION_ID);

        given(projectRepository.findByIdAndStatus(PROJECT_ID, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));

        given(guestSessionRepository.findById(GUEST_SESSION_ID))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> projectRunService.runProject(PROJECT_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("게스트 세션을 찾을 수 없습니다.");

        then(workspaceSessionRepository).should(never())
                .save(any());

        then(containerExecutionService).should(never())
                .start(any(), any(), any());
    }

    private void givenActiveProject() {
        given(projectRepository.findByIdAndStatus(PROJECT_ID, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));

        given(project.getId()).willReturn(PROJECT_ID);
        given(project.getRuntime()).willReturn(runtime);

        given(runtime.getName()).willReturn("nodejs");
        given(runtime.getDisplayName()).willReturn("Node.js");
    }

    private void givenWorkspaceSessionSaved() {
        given(workspaceSessionRepository.save(any(WorkspaceSession.class)))
                .willAnswer(invocation -> {
                    WorkspaceSession workspaceSession = invocation.getArgument(0);
                    ReflectionTestUtils.setField(workspaceSession, "id", WORKSPACE_SESSION_ID);
                    return workspaceSession;
                });
    }

    private void givenContainerInstanceSaved() {
        given(containerInstanceRepository.save(any(ContainerInstance.class)))
                .willAnswer(invocation -> {
                    ContainerInstance containerInstance = invocation.getArgument(0);
                    ReflectionTestUtils.setField(containerInstance, "id", CONTAINER_INSTANCE_ID);
                    return containerInstance;
                });
    }

    private void givenContainerStarted() {
        given(containerExecutionService.start(
                eq(project),
                eq(runtime),
                any(WorkspaceSession.class)
        )).willReturn(containerStartResult);
    }
}