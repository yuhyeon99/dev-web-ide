package com.yuhyeon.devwebide.workspace.repository;

import com.yuhyeon.devwebide.project.domain.Project;
import com.yuhyeon.devwebide.project.domain.ProjectStatus;
import com.yuhyeon.devwebide.project.domain.ProjectType;
import com.yuhyeon.devwebide.project.domain.ProjectVisibility;
import com.yuhyeon.devwebide.runtime.domain.Runtime;
import com.yuhyeon.devwebide.runtime.domain.RuntimeLanguage;
import com.yuhyeon.devwebide.runtime.domain.RuntimeStatus;
import com.yuhyeon.devwebide.user.domain.GuestSession;
import com.yuhyeon.devwebide.user.domain.User;
import com.yuhyeon.devwebide.user.domain.UserRole;
import com.yuhyeon.devwebide.user.domain.UserStatus;
import com.yuhyeon.devwebide.workspace.domain.WorkspaceSession;
import com.yuhyeon.devwebide.workspace.domain.WorkspaceSessionStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class WorkspaceSessionRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @jakarta.annotation.Resource
    private WorkspaceSessionRepository workspaceSessionRepository;

    @Test
    @DisplayName("회원 워크스페이스 세션을 저장한다")
    void saveUserWorkspaceSession() {
        // given
        User user = saveUser("user1@example.com");
        Runtime runtime = saveRuntime("nodejs-1");
        Project project = savePersonalProject(user, runtime, "회원 프로젝트");

        WorkspaceSession workspaceSession = WorkspaceSession.builder()
                .project(project)
                .runtime(runtime)
                .user(user)
                .status(WorkspaceSessionStatus.RUNNING)
                .lastHeartbeatAt(LocalDateTime.now())
                .build();

        // when
        WorkspaceSession savedSession =
                workspaceSessionRepository.save(workspaceSession);

        flushAndClear();

        // then
        Optional<WorkspaceSession> foundSession =
                workspaceSessionRepository.findById(savedSession.getId());

        assertThat(foundSession).isPresent();
        assertThat(foundSession.get().getProject().getId()).isEqualTo(project.getId());
        assertThat(foundSession.get().getRuntime().getId()).isEqualTo(runtime.getId());
        assertThat(foundSession.get().getUser().getId()).isEqualTo(user.getId());
        assertThat(foundSession.get().getGuestSession()).isNull();
        assertThat(foundSession.get().getStatus()).isEqualTo(WorkspaceSessionStatus.RUNNING);
        assertThat(foundSession.get().isUserSession()).isTrue();
        assertThat(foundSession.get().isGuestSession()).isFalse();
    }

    @Test
    @DisplayName("게스트 워크스페이스 세션을 저장한다")
    void saveGuestWorkspaceSession() {
        // given
        GuestSession guestSession = saveGuestSession("guest-token-1");
        Runtime runtime = saveRuntime("python-1");
        Project project = saveGuestProject(guestSession, runtime, "게스트 프로젝트");

        WorkspaceSession workspaceSession = WorkspaceSession.builder()
                .project(project)
                .runtime(runtime)
                .guestSession(guestSession)
                .status(WorkspaceSessionStatus.RUNNING)
                .lastHeartbeatAt(LocalDateTime.now())
                .build();

        // when
        WorkspaceSession savedSession =
                workspaceSessionRepository.save(workspaceSession);

        flushAndClear();

        // then
        Optional<WorkspaceSession> foundSession =
                workspaceSessionRepository.findById(savedSession.getId());

        assertThat(foundSession).isPresent();
        assertThat(foundSession.get().getProject().getId()).isEqualTo(project.getId());
        assertThat(foundSession.get().getRuntime().getId()).isEqualTo(runtime.getId());
        assertThat(foundSession.get().getUser()).isNull();
        assertThat(foundSession.get().getGuestSession().getId()).isEqualTo(guestSession.getId());
        assertThat(foundSession.get().getStatus()).isEqualTo(WorkspaceSessionStatus.RUNNING);
        assertThat(foundSession.get().isUserSession()).isFalse();
        assertThat(foundSession.get().isGuestSession()).isTrue();
    }

    @Test
    @DisplayName("projectId 기준 워크스페이스 세션 목록을 조회한다")
    void findByProjectIdOrderByStartedAtDesc() {
        // given
        User user = saveUser("user2@example.com");
        Runtime runtime = saveRuntime("nodejs-2");
        Project project = savePersonalProject(user, runtime, "프로젝트");

        WorkspaceSession runningSession = saveWorkspaceSession(
                project,
                runtime,
                user,
                null,
                WorkspaceSessionStatus.RUNNING
        );

        WorkspaceSession stoppedSession = saveWorkspaceSession(
                project,
                runtime,
                user,
                null,
                WorkspaceSessionStatus.STOPPED
        );

        flushAndClear();

        // when
        List<WorkspaceSession> sessions =
                workspaceSessionRepository.findByProjectIdOrderByStartedAtDesc(project.getId());

        // then
        assertThat(sessions).hasSize(2);
        assertThat(sessions)
                .extracting(WorkspaceSession::getId)
                .containsExactlyInAnyOrder(
                        runningSession.getId(),
                        stoppedSession.getId()
                );
    }

    @Test
    @DisplayName("projectId와 status 기준 워크스페이스 세션 목록을 조회한다")
    void findByProjectIdAndStatusOrderByStartedAtDesc() {
        // given
        User user = saveUser("user3@example.com");
        Runtime runtime = saveRuntime("nodejs-3");
        Project project = savePersonalProject(user, runtime, "프로젝트");

        WorkspaceSession runningSession = saveWorkspaceSession(
                project,
                runtime,
                user,
                null,
                WorkspaceSessionStatus.RUNNING
        );

        saveWorkspaceSession(
                project,
                runtime,
                user,
                null,
                WorkspaceSessionStatus.STOPPED
        );

        flushAndClear();

        // when
        List<WorkspaceSession> sessions =
                workspaceSessionRepository.findByProjectIdAndStatusOrderByStartedAtDesc(
                        project.getId(),
                        WorkspaceSessionStatus.RUNNING
                );

        // then
        assertThat(sessions).hasSize(1);
        assertThat(sessions.get(0).getId()).isEqualTo(runningSession.getId());
        assertThat(sessions.get(0).getStatus()).isEqualTo(WorkspaceSessionStatus.RUNNING);
    }

    @Test
    @DisplayName("userId와 status 기준 회원 워크스페이스 세션 목록을 조회한다")
    void findByUserIdAndStatusOrderByStartedAtDesc() {
        // given
        User user = saveUser("user4@example.com");
        Runtime runtime = saveRuntime("nodejs-4");
        Project project = savePersonalProject(user, runtime, "회원 프로젝트");

        WorkspaceSession runningSession = saveWorkspaceSession(
                project,
                runtime,
                user,
                null,
                WorkspaceSessionStatus.RUNNING
        );

        saveWorkspaceSession(
                project,
                runtime,
                user,
                null,
                WorkspaceSessionStatus.STOPPED
        );

        flushAndClear();

        // when
        List<WorkspaceSession> sessions =
                workspaceSessionRepository.findByUserIdAndStatusOrderByStartedAtDesc(
                        user.getId(),
                        WorkspaceSessionStatus.RUNNING
                );

        // then
        assertThat(sessions).hasSize(1);
        assertThat(sessions.get(0).getId()).isEqualTo(runningSession.getId());
        assertThat(sessions.get(0).getUser().getId()).isEqualTo(user.getId());
        assertThat(sessions.get(0).getStatus()).isEqualTo(WorkspaceSessionStatus.RUNNING);
    }

    @Test
    @DisplayName("guestSessionId와 status 기준 게스트 워크스페이스 세션 목록을 조회한다")
    void findByGuestSessionIdAndStatusOrderByStartedAtDesc() {
        // given
        GuestSession guestSession = saveGuestSession("guest-token-2");
        Runtime runtime = saveRuntime("python-2");
        Project project = saveGuestProject(guestSession, runtime, "게스트 프로젝트");

        WorkspaceSession runningSession = saveWorkspaceSession(
                project,
                runtime,
                null,
                guestSession,
                WorkspaceSessionStatus.RUNNING
        );

        saveWorkspaceSession(
                project,
                runtime,
                null,
                guestSession,
                WorkspaceSessionStatus.STOPPED
        );

        flushAndClear();

        // when
        List<WorkspaceSession> sessions =
                workspaceSessionRepository.findByGuestSessionIdAndStatusOrderByStartedAtDesc(
                        guestSession.getId(),
                        WorkspaceSessionStatus.RUNNING
                );

        // then
        assertThat(sessions).hasSize(1);
        assertThat(sessions.get(0).getId()).isEqualTo(runningSession.getId());
        assertThat(sessions.get(0).getGuestSession().getId()).isEqualTo(guestSession.getId());
        assertThat(sessions.get(0).getStatus()).isEqualTo(WorkspaceSessionStatus.RUNNING);
    }

    @Test
    @DisplayName("projectId와 status 기준 가장 최근 워크스페이스 세션을 조회한다")
    void findTopByProjectIdAndStatusOrderByStartedAtDesc() {
        // given
        User user = saveUser("user5@example.com");
        Runtime runtime = saveRuntime("nodejs-5");
        Project project = savePersonalProject(user, runtime, "프로젝트");

        WorkspaceSession runningSession = saveWorkspaceSession(
                project,
                runtime,
                user,
                null,
                WorkspaceSessionStatus.RUNNING
        );

        flushAndClear();

        // when
        Optional<WorkspaceSession> foundSession =
                workspaceSessionRepository.findTopByProjectIdAndStatusOrderByStartedAtDesc(
                        project.getId(),
                        WorkspaceSessionStatus.RUNNING
                );

        // then
        assertThat(foundSession).isPresent();
        assertThat(foundSession.get().getId()).isEqualTo(runningSession.getId());
        assertThat(foundSession.get().getStatus()).isEqualTo(WorkspaceSessionStatus.RUNNING);
    }

    @Test
    @DisplayName("회원의 특정 프로젝트 실행 세션 존재 여부를 확인한다")
    void existsByProjectIdAndUserIdAndStatus() {
        // given
        User user = saveUser("user6@example.com");
        Runtime runtime = saveRuntime("nodejs-6");
        Project project = savePersonalProject(user, runtime, "프로젝트");

        saveWorkspaceSession(
                project,
                runtime,
                user,
                null,
                WorkspaceSessionStatus.RUNNING
        );

        flushAndClear();

        // when
        boolean exists = workspaceSessionRepository.existsByProjectIdAndUserIdAndStatus(
                project.getId(),
                user.getId(),
                WorkspaceSessionStatus.RUNNING
        );

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("게스트의 특정 프로젝트 실행 세션 존재 여부를 확인한다")
    void existsByProjectIdAndGuestSessionIdAndStatus() {
        // given
        GuestSession guestSession = saveGuestSession("guest-token-3");
        Runtime runtime = saveRuntime("python-3");
        Project project = saveGuestProject(guestSession, runtime, "게스트 프로젝트");

        saveWorkspaceSession(
                project,
                runtime,
                null,
                guestSession,
                WorkspaceSessionStatus.RUNNING
        );

        flushAndClear();

        // when
        boolean exists = workspaceSessionRepository.existsByProjectIdAndGuestSessionIdAndStatus(
                project.getId(),
                guestSession.getId(),
                WorkspaceSessionStatus.RUNNING
        );

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("워크스페이스 세션을 종료 처리한다")
    void stopWorkspaceSession() {
        // given
        User user = saveUser("user7@example.com");
        Runtime runtime = saveRuntime("nodejs-7");
        Project project = savePersonalProject(user, runtime, "프로젝트");

        WorkspaceSession workspaceSession = saveWorkspaceSession(
                project,
                runtime,
                user,
                null,
                WorkspaceSessionStatus.RUNNING
        );

        LocalDateTime stoppedAt = LocalDateTime.now();

        // when
        workspaceSession.stop(stoppedAt);

        flushAndClear();

        // then
        WorkspaceSession foundSession =
                workspaceSessionRepository.findById(workspaceSession.getId()).orElseThrow();

        assertThat(foundSession.getStatus()).isEqualTo(WorkspaceSessionStatus.STOPPED);
        assertThat(foundSession.getStoppedAt()).isNotNull();
    }

    @Test
    @DisplayName("회원 사용자와 게스트 세션이 둘 다 없으면 예외가 발생한다")
    void throwsExceptionWhenUserAndGuestSessionAreBothNull() {
        // given
        User user = saveUser("user8@example.com");
        Runtime runtime = saveRuntime("nodejs-8");
        Project project = savePersonalProject(user, runtime, "프로젝트");

        // when & then
        assertThatThrownBy(() -> WorkspaceSession.builder()
                .project(project)
                .runtime(runtime)
                .status(WorkspaceSessionStatus.RUNNING)
                .lastHeartbeatAt(LocalDateTime.now())
                .build()
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 사용자 또는 게스트 세션 중 하나만 지정해야 합니다.");
    }

    @Test
    @DisplayName("회원 사용자와 게스트 세션이 둘 다 있으면 예외가 발생한다")
    void throwsExceptionWhenUserAndGuestSessionBothExist() {
        // given
        User user = saveUser("user9@example.com");
        GuestSession guestSession = saveGuestSession("guest-token-4");
        Runtime runtime = saveRuntime("nodejs-9");
        Project project = savePersonalProject(user, runtime, "프로젝트");

        // when & then
        assertThatThrownBy(() -> WorkspaceSession.builder()
                .project(project)
                .runtime(runtime)
                .user(user)
                .guestSession(guestSession)
                .status(WorkspaceSessionStatus.RUNNING)
                .lastHeartbeatAt(LocalDateTime.now())
                .build()
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 사용자 또는 게스트 세션 중 하나만 지정해야 합니다.");
    }

    private User saveUser(String email) {
        User user = User.builder()
                .email(email)
                .nickname("테스트 사용자")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        entityManager.persist(user);
        return user;
    }

    private GuestSession saveGuestSession(String guestToken) {
        GuestSession guestSession = GuestSession.builder()
                .guestToken(guestToken)
                .clientIp("127.0.0.1")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        entityManager.persist(guestSession);
        return guestSession;
    }

    private Runtime saveRuntime(String name) {
        Runtime runtime = Runtime.builder()
                .name(name)
                .displayName("Node.js")
                .version("20")
                .dockerImage("node:20")
                .language(RuntimeLanguage.JAVA)
                .status(RuntimeStatus.ACTIVE)
                .build();

        entityManager.persist(runtime);
        return runtime;
    }

    private Project savePersonalProject(
            User ownerUser,
            Runtime runtime,
            String name
    ) {
        Project project = Project.builder()
                .ownerUser(ownerUser)
                .runtime(runtime)
                .name(name)
                .description("테스트 프로젝트")
                .projectType(ProjectType.PERSONAL)
                .visibility(ProjectVisibility.PRIVATE)
                .status(ProjectStatus.ACTIVE)
                .storagePath("/projects/test")
                .build();

        entityManager.persist(project);
        return project;
    }

    private Project saveGuestProject(
            GuestSession guestSession,
            Runtime runtime,
            String name
    ) {
        Project project = Project.builder()
                .guestSession(guestSession)
                .runtime(runtime)
                .name(name)
                .description("게스트 테스트 프로젝트")
                .projectType(ProjectType.GUEST)
                .visibility(ProjectVisibility.PRIVATE)
                .status(ProjectStatus.ACTIVE)
                .storagePath("/projects/guest-test")
                .build();

        entityManager.persist(project);
        return project;
    }

    private WorkspaceSession saveWorkspaceSession(
            Project project,
            Runtime runtime,
            User user,
            GuestSession guestSession,
            WorkspaceSessionStatus status
    ) {
        WorkspaceSession workspaceSession = WorkspaceSession.builder()
                .project(project)
                .runtime(runtime)
                .user(user)
                .guestSession(guestSession)
                .status(status)
                .lastHeartbeatAt(LocalDateTime.now())
                .build();

        workspaceSessionRepository.save(workspaceSession);
        return workspaceSession;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}