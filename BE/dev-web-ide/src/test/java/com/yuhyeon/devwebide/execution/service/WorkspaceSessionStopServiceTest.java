package com.yuhyeon.devwebide.execution.service;

import com.yuhyeon.devwebide.execution.domain.ContainerInstance;
import com.yuhyeon.devwebide.execution.domain.ContainerInstanceStatus;
import com.yuhyeon.devwebide.execution.dto.WorkspaceSessionStopResponse;
import com.yuhyeon.devwebide.execution.repository.ContainerInstanceRepository;
import com.yuhyeon.devwebide.project.domain.Project;
import com.yuhyeon.devwebide.runtime.domain.Runtime;
import com.yuhyeon.devwebide.user.domain.User;
import com.yuhyeon.devwebide.workspace.domain.WorkspaceSession;
import com.yuhyeon.devwebide.workspace.domain.WorkspaceSessionStatus;
import com.yuhyeon.devwebide.workspace.repository.WorkspaceSessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class WorkspaceSessionStopServiceTest {

    private static final Long WORKSPACE_SESSION_ID = 100L;
    private static final Long CONTAINER_INSTANCE_ID = 200L;

    @Mock
    private WorkspaceSessionRepository workspaceSessionRepository;

    @Mock
    private ContainerInstanceRepository containerInstanceRepository;

    @Mock
    private ContainerExecutionService containerExecutionService;

    @InjectMocks
    private WorkspaceSessionStopService workspaceSessionStopService;

    @Test
    @DisplayName("실행 중인 워크스페이스 세션을 중지한다")
    void stopRunningWorkspaceSession() {
        // given
        WorkspaceSession workspaceSession = workspaceSession(WorkspaceSessionStatus.RUNNING);
        ContainerInstance containerInstance = runningContainerInstance(workspaceSession);

        givenRunningWorkspaceSession(workspaceSession);
        givenRunningContainerInstance(containerInstance);

        // when
        WorkspaceSessionStopResponse response =
                workspaceSessionStopService.stopWorkspaceSession(WORKSPACE_SESSION_ID);

        // then
        assertThat(workspaceSession.getStatus()).isEqualTo(WorkspaceSessionStatus.STOPPED);
        assertThat(containerInstance.getStatus()).isEqualTo(ContainerInstanceStatus.STOPPED);
        assertThat(workspaceSession.getStoppedAt()).isNotNull();
        assertThat(containerInstance.getStoppedAt()).isNotNull();
        assertThat(containerInstance.getStoppedAt()).isEqualTo(workspaceSession.getStoppedAt());

        assertThat(response.workspaceSessionId()).isEqualTo(WORKSPACE_SESSION_ID);
        assertThat(response.containerInstanceId()).isEqualTo(CONTAINER_INSTANCE_ID);
        assertThat(response.workspaceStatus()).isEqualTo(WorkspaceSessionStatus.STOPPED);
        assertThat(response.containerStatus()).isEqualTo(ContainerInstanceStatus.STOPPED);
        assertThat(response.stoppedAt()).isEqualTo(workspaceSession.getStoppedAt());
    }

    @Test
    @DisplayName("컨테이너 실행 서비스에 중지를 요청한다")
    void callContainerExecutionServiceStop() {
        // given
        WorkspaceSession workspaceSession = workspaceSession(WorkspaceSessionStatus.RUNNING);
        ContainerInstance containerInstance = runningContainerInstance(workspaceSession);

        givenRunningWorkspaceSession(workspaceSession);
        givenRunningContainerInstance(containerInstance);

        // when
        workspaceSessionStopService.stopWorkspaceSession(WORKSPACE_SESSION_ID);

        // then
        then(containerExecutionService).should()
                .stop(containerInstance);
    }

    @Test
    @DisplayName("존재하지 않는 워크스페이스 세션이면 예외가 발생한다")
    void throwExceptionWhenWorkspaceSessionNotFound() {
        // given
        given(workspaceSessionRepository.findById(WORKSPACE_SESSION_ID))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> workspaceSessionStopService.stopWorkspaceSession(WORKSPACE_SESSION_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("워크스페이스 세션을 찾을 수 없습니다.");

        then(containerInstanceRepository).shouldHaveNoInteractions();
        then(containerExecutionService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("이미 중지된 워크스페이스 세션이면 예외가 발생한다")
    void throwExceptionWhenWorkspaceSessionAlreadyStopped() {
        // given
        WorkspaceSession workspaceSession = workspaceSession(WorkspaceSessionStatus.STOPPED);

        given(workspaceSessionRepository.findById(WORKSPACE_SESSION_ID))
                .willReturn(Optional.of(workspaceSession));

        // when & then
        assertThatThrownBy(() -> workspaceSessionStopService.stopWorkspaceSession(WORKSPACE_SESSION_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("실행 중인 워크스페이스 세션만 중지할 수 있습니다.");

        then(containerInstanceRepository).shouldHaveNoInteractions();
        then(containerExecutionService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("실패 상태의 워크스페이스 세션이면 예외가 발생한다")
    void throwExceptionWhenWorkspaceSessionFailed() {
        // given
        WorkspaceSession workspaceSession = workspaceSession(WorkspaceSessionStatus.FAILED);

        given(workspaceSessionRepository.findById(WORKSPACE_SESSION_ID))
                .willReturn(Optional.of(workspaceSession));

        // when & then
        assertThatThrownBy(() -> workspaceSessionStopService.stopWorkspaceSession(WORKSPACE_SESSION_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("실행 중인 워크스페이스 세션만 중지할 수 있습니다.");

        then(containerInstanceRepository).shouldHaveNoInteractions();
        then(containerExecutionService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("시작 중인 워크스페이스 세션이면 예외가 발생한다")
    void throwExceptionWhenWorkspaceSessionStarting() {
        // given
        WorkspaceSession workspaceSession = workspaceSession(WorkspaceSessionStatus.STARTING);

        given(workspaceSessionRepository.findById(WORKSPACE_SESSION_ID))
                .willReturn(Optional.of(workspaceSession));

        // when & then
        assertThatThrownBy(() -> workspaceSessionStopService.stopWorkspaceSession(WORKSPACE_SESSION_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("실행 중인 워크스페이스 세션만 중지할 수 있습니다.");

        then(containerInstanceRepository).shouldHaveNoInteractions();
        then(containerExecutionService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("실행 중인 컨테이너가 없으면 예외가 발생한다")
    void throwExceptionWhenRunningContainerNotFound() {
        // given
        WorkspaceSession workspaceSession = workspaceSession(WorkspaceSessionStatus.RUNNING);

        givenRunningWorkspaceSession(workspaceSession);

        given(containerInstanceRepository.findTopByWorkspaceSessionIdAndStatusOrderByCreatedAtDesc(
                WORKSPACE_SESSION_ID,
                ContainerInstanceStatus.RUNNING
        )).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> workspaceSessionStopService.stopWorkspaceSession(WORKSPACE_SESSION_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("실행 중인 컨테이너를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("실행 중인 컨테이너가 없으면 컨테이너 중지를 요청하지 않는다")
    void doNotStopContainerWhenRunningContainerNotFound() {
        // given
        WorkspaceSession workspaceSession = workspaceSession(WorkspaceSessionStatus.RUNNING);

        givenRunningWorkspaceSession(workspaceSession);

        given(containerInstanceRepository.findTopByWorkspaceSessionIdAndStatusOrderByCreatedAtDesc(
                WORKSPACE_SESSION_ID,
                ContainerInstanceStatus.RUNNING
        )).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> workspaceSessionStopService.stopWorkspaceSession(WORKSPACE_SESSION_ID))
                .isInstanceOf(IllegalArgumentException.class);

        then(containerExecutionService).should(never())
                .stop(any());
    }

    @Test
    @DisplayName("중지 응답 필드를 반환한다")
    void returnWorkspaceSessionStopResponse() {
        // given
        WorkspaceSession workspaceSession = workspaceSession(WorkspaceSessionStatus.RUNNING);
        ContainerInstance containerInstance = runningContainerInstance(workspaceSession);

        givenRunningWorkspaceSession(workspaceSession);
        givenRunningContainerInstance(containerInstance);

        // when
        WorkspaceSessionStopResponse response =
                workspaceSessionStopService.stopWorkspaceSession(WORKSPACE_SESSION_ID);

        // then
        assertThat(response.workspaceSessionId()).isEqualTo(WORKSPACE_SESSION_ID);
        assertThat(response.containerInstanceId()).isEqualTo(CONTAINER_INSTANCE_ID);
        assertThat(response.workspaceStatus()).isEqualTo(WorkspaceSessionStatus.STOPPED);
        assertThat(response.containerStatus()).isEqualTo(ContainerInstanceStatus.STOPPED);
        assertThat(response.stoppedAt()).isNotNull();
    }

    private void givenRunningWorkspaceSession(WorkspaceSession workspaceSession) {
        given(workspaceSessionRepository.findById(WORKSPACE_SESSION_ID))
                .willReturn(Optional.of(workspaceSession));
    }

    private void givenRunningContainerInstance(ContainerInstance containerInstance) {
        given(containerInstanceRepository.findTopByWorkspaceSessionIdAndStatusOrderByCreatedAtDesc(
                WORKSPACE_SESSION_ID,
                ContainerInstanceStatus.RUNNING
        )).willReturn(Optional.of(containerInstance));
    }

    private WorkspaceSession workspaceSession(WorkspaceSessionStatus status) {
        WorkspaceSession workspaceSession = WorkspaceSession.builder()
                .project(mock(Project.class))
                .runtime(mock(Runtime.class))
                .user(mock(User.class))
                .status(status)
                .build();

        ReflectionTestUtils.setField(workspaceSession, "id", WORKSPACE_SESSION_ID);

        return workspaceSession;
    }

    private ContainerInstance runningContainerInstance(WorkspaceSession workspaceSession) {
        ContainerInstance containerInstance = ContainerInstance.builder()
                .workspaceSession(workspaceSession)
                .provider("LOCAL")
                .containerId("local-container-1")
                .dockerImage("node:20")
                .status(ContainerInstanceStatus.RUNNING)
                .efsMountPath("/projects/1")
                .build();

        ReflectionTestUtils.setField(containerInstance, "id", CONTAINER_INSTANCE_ID);

        return containerInstance;
    }
}
