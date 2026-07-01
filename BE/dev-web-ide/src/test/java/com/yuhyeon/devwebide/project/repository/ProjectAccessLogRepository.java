package com.yuhyeon.devwebide.project.repository;

import com.yuhyeon.devwebide.project.domain.Project;
import com.yuhyeon.devwebide.project.domain.ProjectAccessLog;
import com.yuhyeon.devwebide.project.domain.ProjectAccessType;
import com.yuhyeon.devwebide.project.domain.ProjectStatus;
import com.yuhyeon.devwebide.project.domain.ProjectType;
import com.yuhyeon.devwebide.project.domain.ProjectVisibility;
import com.yuhyeon.devwebide.runtime.domain.Runtime;
import com.yuhyeon.devwebide.runtime.domain.RuntimeLanguage;
import com.yuhyeon.devwebide.runtime.domain.RuntimeStatus;
import com.yuhyeon.devwebide.runtime.repository.RuntimeRepository;
import com.yuhyeon.devwebide.user.domain.GuestSession;
import com.yuhyeon.devwebide.user.domain.User;
import com.yuhyeon.devwebide.user.domain.UserRole;
import com.yuhyeon.devwebide.user.domain.UserStatus;
import com.yuhyeon.devwebide.user.repository.GuestSessionRepository;
import com.yuhyeon.devwebide.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class ProjectAccessLogRepositoryTest {

    @Autowired
    private ProjectAccessLogRepository projectAccessLogRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GuestSessionRepository guestSessionRepository;

    @Autowired
    private RuntimeRepository runtimeRepository;

    @Test
    @DisplayName("회원 프로젝트 접근 로그 저장")
    void saveUserProjectAccessLog() {
        Runtime runtime = createRuntime("node-20-user-log");
        User user = createUser("user-log@test.com", "회원로그테스터");
        Project project = createUserProject(user, runtime, "회원 프로젝트");

        ProjectAccessLog accessLog = ProjectAccessLog.builder()
                .project(project)
                .user(user)
                .guestSession(null)
                .accessType(ProjectAccessType.OPEN)
                .build();

        ProjectAccessLog savedAccessLog =
                projectAccessLogRepository.save(accessLog);

        assertThat(savedAccessLog.getId()).isNotNull();
        assertThat(savedAccessLog.getProject().getId())
                .isEqualTo(project.getId());
        assertThat(savedAccessLog.getUser().getId())
                .isEqualTo(user.getId());
        assertThat(savedAccessLog.getGuestSession()).isNull();
        assertThat(savedAccessLog.getAccessType())
                .isEqualTo(ProjectAccessType.OPEN);
        assertThat(savedAccessLog.getAccessedAt()).isNotNull();
        assertThat(savedAccessLog.isUserAccess()).isTrue();
        assertThat(savedAccessLog.isGuestAccess()).isFalse();
        assertThat(savedAccessLog.isOpenAccess()).isTrue();
    }

    @Test
    @DisplayName("게스트 프로젝트 접근 로그 저장")
    void saveGuestProjectAccessLog() {
        Runtime runtime = createRuntime("node-20-guest-log");
        GuestSession guestSession = createGuestSession("guest-token-log");
        Project project = createGuestProject(guestSession, runtime, "게스트 프로젝트");

        ProjectAccessLog accessLog = ProjectAccessLog.builder()
                .project(project)
                .user(null)
                .guestSession(guestSession)
                .accessType(ProjectAccessType.OPEN)
                .build();

        ProjectAccessLog savedAccessLog =
                projectAccessLogRepository.save(accessLog);

        assertThat(savedAccessLog.getId()).isNotNull();
        assertThat(savedAccessLog.getProject().getId())
                .isEqualTo(project.getId());
        assertThat(savedAccessLog.getUser()).isNull();
        assertThat(savedAccessLog.getGuestSession().getId())
                .isEqualTo(guestSession.getId());
        assertThat(savedAccessLog.getAccessType())
                .isEqualTo(ProjectAccessType.OPEN);
        assertThat(savedAccessLog.getAccessedAt()).isNotNull();
        assertThat(savedAccessLog.isUserAccess()).isFalse();
        assertThat(savedAccessLog.isGuestAccess()).isTrue();
    }

    @Test
    @DisplayName("회원 ID와 접근 유형으로 프로젝트 접근 기록 조회")
    void findByUserIdAndAccessTypeOrderByAccessedAtDesc() {
        Runtime runtime = createRuntime("node-20-user-recent");
        User user = createUser("user-recent@test.com", "회원최근테스터");

        Project firstProject = createUserProject(user, runtime, "첫 번째 프로젝트");
        Project secondProject = createUserProject(user, runtime, "두 번째 프로젝트");

        projectAccessLogRepository.save(ProjectAccessLog.builder()
                .project(firstProject)
                .user(user)
                .guestSession(null)
                .accessType(ProjectAccessType.OPEN)
                .build());

        projectAccessLogRepository.save(ProjectAccessLog.builder()
                .project(secondProject)
                .user(user)
                .guestSession(null)
                .accessType(ProjectAccessType.OPEN)
                .build());

        List<ProjectAccessLog> result =
                projectAccessLogRepository.findByUserIdAndAccessTypeOrderByAccessedAtDesc(
                        user.getId(),
                        ProjectAccessType.OPEN
                );

        assertThat(result).hasSize(2);
        assertThat(result)
                .allMatch(ProjectAccessLog::isUserAccess)
                .allMatch(ProjectAccessLog::isOpenAccess);
    }

    @Test
    @DisplayName("게스트 세션 ID와 접근 유형으로 프로젝트 접근 기록 조회")
    void findByGuestSessionIdAndAccessTypeOrderByAccessedAtDesc() {
        Runtime runtime = createRuntime("node-20-guest-recent");
        GuestSession guestSession = createGuestSession("guest-token-recent");

        Project firstProject = createGuestProject(guestSession, runtime, "첫 번째 게스트 프로젝트");
        Project secondProject = createGuestProject(guestSession, runtime, "두 번째 게스트 프로젝트");

        projectAccessLogRepository.save(ProjectAccessLog.builder()
                .project(firstProject)
                .user(null)
                .guestSession(guestSession)
                .accessType(ProjectAccessType.OPEN)
                .build());

        projectAccessLogRepository.save(ProjectAccessLog.builder()
                .project(secondProject)
                .user(null)
                .guestSession(guestSession)
                .accessType(ProjectAccessType.OPEN)
                .build());

        List<ProjectAccessLog> result =
                projectAccessLogRepository.findByGuestSessionIdAndAccessTypeOrderByAccessedAtDesc(
                        guestSession.getId(),
                        ProjectAccessType.OPEN
                );

        assertThat(result).hasSize(2);
        assertThat(result)
                .allMatch(ProjectAccessLog::isGuestAccess)
                .allMatch(ProjectAccessLog::isOpenAccess);
    }

    @Test
    @DisplayName("프로젝트 ID로 접근 기록 조회")
    void findByProjectIdOrderByAccessedAtDesc() {
        Runtime runtime = createRuntime("node-20-project-log");
        User user = createUser("project-log@test.com", "프로젝트로그테스터");
        Project project = createUserProject(user, runtime, "접근 기록 프로젝트");

        projectAccessLogRepository.save(ProjectAccessLog.builder()
                .project(project)
                .user(user)
                .guestSession(null)
                .accessType(ProjectAccessType.OPEN)
                .build());

        projectAccessLogRepository.save(ProjectAccessLog.builder()
                .project(project)
                .user(user)
                .guestSession(null)
                .accessType(ProjectAccessType.RUN)
                .build());

        List<ProjectAccessLog> result =
                projectAccessLogRepository.findByProjectIdOrderByAccessedAtDesc(
                        project.getId()
                );

        assertThat(result).hasSize(2);
        assertThat(result)
                .allMatch(accessLog ->
                        accessLog.getProject().getId().equals(project.getId())
                );
    }

    @Test
    @DisplayName("프로젝트 ID와 접근 유형으로 접근 기록 조회")
    void findByProjectIdAndAccessTypeOrderByAccessedAtDesc() {
        Runtime runtime = createRuntime("node-20-project-type-log");
        User user = createUser("project-type-log@test.com", "프로젝트유형로그테스터");
        Project project = createUserProject(user, runtime, "접근 유형 프로젝트");

        projectAccessLogRepository.save(ProjectAccessLog.builder()
                .project(project)
                .user(user)
                .guestSession(null)
                .accessType(ProjectAccessType.OPEN)
                .build());

        projectAccessLogRepository.save(ProjectAccessLog.builder()
                .project(project)
                .user(user)
                .guestSession(null)
                .accessType(ProjectAccessType.RUN)
                .build());

        List<ProjectAccessLog> result =
                projectAccessLogRepository.findByProjectIdAndAccessTypeOrderByAccessedAtDesc(
                        project.getId(),
                        ProjectAccessType.OPEN
                );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProject().getId())
                .isEqualTo(project.getId());
        assertThat(result.get(0).getAccessType())
                .isEqualTo(ProjectAccessType.OPEN);
    }

    @Test
    @DisplayName("회원 사용자와 게스트 세션이 모두 없으면 예외 발생")
    void createAccessLogWithoutOwner() {
        Runtime runtime = createRuntime("node-20-no-owner");
        User user = createUser("no-owner@test.com", "소유자없음테스터");
        Project project = createUserProject(user, runtime, "소유자 검증 프로젝트");

        assertThatThrownBy(() -> ProjectAccessLog.builder()
                .project(project)
                .user(null)
                .guestSession(null)
                .accessType(ProjectAccessType.OPEN)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 사용자 또는 게스트 세션 중 하나만 지정해야 합니다.");
    }

    @Test
    @DisplayName("회원 사용자와 게스트 세션이 모두 있으면 예외 발생")
    void createAccessLogWithBothUserAndGuestSession() {
        Runtime runtime = createRuntime("node-20-both-owner");
        User user = createUser("both-owner@test.com", "중복소유자테스터");
        GuestSession guestSession = createGuestSession("guest-token-both-owner");
        Project project = createUserProject(user, runtime, "중복 소유자 검증 프로젝트");

        assertThatThrownBy(() -> ProjectAccessLog.builder()
                .project(project)
                .user(user)
                .guestSession(guestSession)
                .accessType(ProjectAccessType.OPEN)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 사용자 또는 게스트 세션 중 하나만 지정해야 합니다.");
    }

    private Runtime createRuntime(String name) {
        Runtime runtime = Runtime.builder()
                .name(name)
                .displayName("Node.js 20")
                .version("20")
                .dockerImage("node:20")
                .language(RuntimeLanguage.NODE)
                .status(RuntimeStatus.ACTIVE)
                .build();

        return runtimeRepository.save(runtime);
    }

    private User createUser(String email, String nickname) {
        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        return userRepository.save(user);
    }

    private GuestSession createGuestSession(String guestToken) {
        GuestSession guestSession = GuestSession.builder()
                .guestToken(guestToken)
                .clientIp("127.0.0.1")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        return guestSessionRepository.save(guestSession);
    }

    private Project createUserProject(
            User user,
            Runtime runtime,
            String name
    ) {
        Project project = Project.builder()
                .ownerUser(user)
                .guestSession(null)
                .runtime(runtime)
                .name(name)
                .description("테스트 프로젝트입니다.")
                .projectType(ProjectType.PERSONAL)
                .visibility(ProjectVisibility.PRIVATE)
                .status(ProjectStatus.ACTIVE)
                .storagePath("/projects/" + name)
                .build();

        return projectRepository.save(project);
    }

    private Project createGuestProject(
            GuestSession guestSession,
            Runtime runtime,
            String name
    ) {
        Project project = Project.builder()
                .ownerUser(null)
                .guestSession(guestSession)
                .runtime(runtime)
                .name(name)
                .description("게스트 테스트 프로젝트입니다.")
                .projectType(ProjectType.GUEST)
                .visibility(ProjectVisibility.PRIVATE)
                .status(ProjectStatus.ACTIVE)
                .storagePath("/projects/" + name)
                .build();

        return projectRepository.save(project);
    }
}