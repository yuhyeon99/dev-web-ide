package com.yuhyeon.devwebide.project.repository;

import com.yuhyeon.devwebide.project.domain.Project;
import com.yuhyeon.devwebide.project.domain.ProjectSaveBatch;
import com.yuhyeon.devwebide.project.domain.ProjectSaveBatchStatus;
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

@DataJpaTest
class ProjectSaveBatchRepositoryTest {

    @Autowired
    private ProjectSaveBatchRepository projectSaveBatchRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GuestSessionRepository guestSessionRepository;

    @Autowired
    private RuntimeRepository runtimeRepository;

    @Test
    @DisplayName("회원 프로젝트 저장 배치를 저장한다")
    void saveUserProjectSaveBatch() {
        // given
        User user = saveUser("user1@test.com", "회원1");
        Runtime runtime = saveRuntime("nodejs-20");
        Project project = savePersonalProject(user, runtime, "회원 프로젝트");

        ProjectSaveBatch saveBatch = ProjectSaveBatch.builder()
                .project(project)
                .user(user)
                .guestSession(null)
                .status(ProjectSaveBatchStatus.SUCCESS)
                .savedFileCount(2)
                .build();

        // when
        ProjectSaveBatch savedBatch = projectSaveBatchRepository.save(saveBatch);

        // then
        assertThat(savedBatch.getId()).isNotNull();
        assertThat(savedBatch.getProject().getId()).isEqualTo(project.getId());
        assertThat(savedBatch.getUser().getId()).isEqualTo(user.getId());
        assertThat(savedBatch.getGuestSession()).isNull();
        assertThat(savedBatch.getStatus()).isEqualTo(ProjectSaveBatchStatus.SUCCESS);
        assertThat(savedBatch.getSavedFileCount()).isEqualTo(2);
        assertThat(savedBatch.getSavedAt()).isNotNull();
        assertThat(savedBatch.isUserSaveBatch()).isTrue();
        assertThat(savedBatch.isGuestSaveBatch()).isFalse();
        assertThat(savedBatch.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("게스트 프로젝트 저장 배치를 저장한다")
    void saveGuestProjectSaveBatch() {
        // given
        GuestSession guestSession = saveGuestSession("guest-token-1");
        Runtime runtime = saveRuntime("python-3-12");
        Project project = saveGuestProject(guestSession, runtime, "게스트 프로젝트");

        ProjectSaveBatch saveBatch = ProjectSaveBatch.builder()
                .project(project)
                .user(null)
                .guestSession(guestSession)
                .status(ProjectSaveBatchStatus.SUCCESS)
                .savedFileCount(1)
                .build();

        // when
        ProjectSaveBatch savedBatch = projectSaveBatchRepository.save(saveBatch);

        // then
        assertThat(savedBatch.getId()).isNotNull();
        assertThat(savedBatch.getProject().getId()).isEqualTo(project.getId());
        assertThat(savedBatch.getUser()).isNull();
        assertThat(savedBatch.getGuestSession().getId()).isEqualTo(guestSession.getId());
        assertThat(savedBatch.getStatus()).isEqualTo(ProjectSaveBatchStatus.SUCCESS);
        assertThat(savedBatch.getSavedFileCount()).isEqualTo(1);
        assertThat(savedBatch.getSavedAt()).isNotNull();
        assertThat(savedBatch.isUserSaveBatch()).isFalse();
        assertThat(savedBatch.isGuestSaveBatch()).isTrue();
    }

    @Test
    @DisplayName("프로젝트 ID 기준 저장 배치 목록을 최근 저장 순으로 조회한다")
    void findByProjectIdOrderBySavedAtDesc() {
        // given
        User user = saveUser("user2@test.com", "회원2");
        Runtime runtime = saveRuntime("java-21");
        Project project = savePersonalProject(user, runtime, "저장 이력 프로젝트");

        ProjectSaveBatch firstBatch = saveUserSaveBatch(
                project,
                user,
                ProjectSaveBatchStatus.SUCCESS,
                1
        );

        ProjectSaveBatch secondBatch = saveUserSaveBatch(
                project,
                user,
                ProjectSaveBatchStatus.PARTIAL,
                2
        );

        // when
        List<ProjectSaveBatch> result =
                projectSaveBatchRepository.findByProjectIdOrderBySavedAtDesc(project.getId());

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(ProjectSaveBatch::getId)
                .contains(firstBatch.getId(), secondBatch.getId());
    }

    @Test
    @DisplayName("프로젝트 ID와 저장 상태 기준 저장 배치 목록을 조회한다")
    void findByProjectIdAndStatusOrderBySavedAtDesc() {
        // given
        User user = saveUser("user3@test.com", "회원3");
        Runtime runtime = saveRuntime("cpp-17");
        Project project = savePersonalProject(user, runtime, "상태 조회 프로젝트");

        ProjectSaveBatch successBatch = saveUserSaveBatch(
                project,
                user,
                ProjectSaveBatchStatus.SUCCESS,
                3
        );

        saveUserSaveBatch(
                project,
                user,
                ProjectSaveBatchStatus.FAILED,
                0
        );

        // when
        List<ProjectSaveBatch> result =
                projectSaveBatchRepository.findByProjectIdAndStatusOrderBySavedAtDesc(
                        project.getId(),
                        ProjectSaveBatchStatus.SUCCESS
                );

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(successBatch.getId());
        assertThat(result.get(0).getStatus()).isEqualTo(ProjectSaveBatchStatus.SUCCESS);
    }

    @Test
    @DisplayName("회원 사용자 ID 기준 저장 배치 목록을 조회한다")
    void findByUserIdOrderBySavedAtDesc() {
        // given
        User user = saveUser("user4@test.com", "회원4");
        Runtime runtime = saveRuntime("nodejs-18");
        Project project = savePersonalProject(user, runtime, "회원 저장 조회 프로젝트");

        ProjectSaveBatch saveBatch = saveUserSaveBatch(
                project,
                user,
                ProjectSaveBatchStatus.SUCCESS,
                2
        );

        // when
        List<ProjectSaveBatch> result =
                projectSaveBatchRepository.findByUserIdOrderBySavedAtDesc(user.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(saveBatch.getId());
        assertThat(result.get(0).getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("회원 사용자 ID와 저장 상태 기준 저장 배치 목록을 조회한다")
    void findByUserIdAndStatusOrderBySavedAtDesc() {
        // given
        User user = saveUser("user5@test.com", "회원5");
        Runtime runtime = saveRuntime("python-3-11");
        Project project = savePersonalProject(user, runtime, "회원 상태 조회 프로젝트");

        ProjectSaveBatch successBatch = saveUserSaveBatch(
                project,
                user,
                ProjectSaveBatchStatus.SUCCESS,
                2
        );

        saveUserSaveBatch(
                project,
                user,
                ProjectSaveBatchStatus.FAILED,
                0
        );

        // when
        List<ProjectSaveBatch> result =
                projectSaveBatchRepository.findByUserIdAndStatusOrderBySavedAtDesc(
                        user.getId(),
                        ProjectSaveBatchStatus.SUCCESS
                );

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(successBatch.getId());
        assertThat(result.get(0).getStatus()).isEqualTo(ProjectSaveBatchStatus.SUCCESS);
    }

    @Test
    @DisplayName("게스트 세션 ID 기준 저장 배치 목록을 조회한다")
    void findByGuestSessionIdOrderBySavedAtDesc() {
        // given
        GuestSession guestSession = saveGuestSession("guest-token-2");
        Runtime runtime = saveRuntime("nodejs-22");
        Project project = saveGuestProject(guestSession, runtime, "게스트 저장 조회 프로젝트");

        ProjectSaveBatch saveBatch = saveGuestSaveBatch(
                project,
                guestSession,
                ProjectSaveBatchStatus.SUCCESS,
                1
        );

        // when
        List<ProjectSaveBatch> result =
                projectSaveBatchRepository.findByGuestSessionIdOrderBySavedAtDesc(
                        guestSession.getId()
                );

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(saveBatch.getId());
        assertThat(result.get(0).getGuestSession().getId()).isEqualTo(guestSession.getId());
    }

    @Test
    @DisplayName("게스트 세션 ID와 저장 상태 기준 저장 배치 목록을 조회한다")
    void findByGuestSessionIdAndStatusOrderBySavedAtDesc() {
        // given
        GuestSession guestSession = saveGuestSession("guest-token-3");
        Runtime runtime = saveRuntime("java-17");
        Project project = saveGuestProject(guestSession, runtime, "게스트 상태 조회 프로젝트");

        ProjectSaveBatch partialBatch = saveGuestSaveBatch(
                project,
                guestSession,
                ProjectSaveBatchStatus.PARTIAL,
                1
        );

        saveGuestSaveBatch(
                project,
                guestSession,
                ProjectSaveBatchStatus.FAILED,
                0
        );

        // when
        List<ProjectSaveBatch> result =
                projectSaveBatchRepository.findByGuestSessionIdAndStatusOrderBySavedAtDesc(
                        guestSession.getId(),
                        ProjectSaveBatchStatus.PARTIAL
                );

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(partialBatch.getId());
        assertThat(result.get(0).getStatus()).isEqualTo(ProjectSaveBatchStatus.PARTIAL);
    }

    @Test
    @DisplayName("프로젝트의 가장 최근 저장 배치를 조회한다")
    void findTopByProjectIdOrderBySavedAtDesc() {
        // given
        User user = saveUser("user6@test.com", "회원6");
        Runtime runtime = saveRuntime("python-3-10");
        Project project = savePersonalProject(user, runtime, "최근 저장 조회 프로젝트");

        ProjectSaveBatch saveBatch = saveUserSaveBatch(
                project,
                user,
                ProjectSaveBatchStatus.SUCCESS,
                1
        );

        // when
        ProjectSaveBatch result =
                projectSaveBatchRepository.findTopByProjectIdOrderBySavedAtDesc(project.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(saveBatch.getId());
    }

    private User saveUser(String email, String nickname) {
        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        return userRepository.save(user);
    }

    private GuestSession saveGuestSession(String guestToken) {
        GuestSession guestSession = GuestSession.builder()
                .guestToken(guestToken)
                .clientIp("127.0.0.1")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        return guestSessionRepository.save(guestSession);
    }

    private Runtime saveRuntime(String name) {
        Runtime runtime = Runtime.builder()
                .name(name)
                .displayName(name)
                .version("1.0.0")
                .dockerImage("test/" + name)
                .language(RuntimeLanguage.NODE)
                .status(RuntimeStatus.ACTIVE)
                .build();

        return runtimeRepository.save(runtime);
    }

    private Project savePersonalProject(
            User ownerUser,
            Runtime runtime,
            String name
    ) {
        Project project = Project.builder()
                .ownerUser(ownerUser)
                .guestSession(null)
                .runtime(runtime)
                .name(name)
                .description("테스트 프로젝트입니다.")
                .projectType(ProjectType.PERSONAL)
                .visibility(ProjectVisibility.PRIVATE)
                .status(ProjectStatus.ACTIVE)
                .storagePath("/projects/test/" + name)
                .build();

        return projectRepository.save(project);
    }

    private Project saveGuestProject(
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
                .storagePath("/projects/test/" + name)
                .build();

        return projectRepository.save(project);
    }

    private ProjectSaveBatch saveUserSaveBatch(
            Project project,
            User user,
            ProjectSaveBatchStatus status,
            Integer savedFileCount
    ) {
        ProjectSaveBatch saveBatch = ProjectSaveBatch.builder()
                .project(project)
                .user(user)
                .guestSession(null)
                .status(status)
                .savedFileCount(savedFileCount)
                .build();

        return projectSaveBatchRepository.save(saveBatch);
    }

    private ProjectSaveBatch saveGuestSaveBatch(
            Project project,
            GuestSession guestSession,
            ProjectSaveBatchStatus status,
            Integer savedFileCount
    ) {
        ProjectSaveBatch saveBatch = ProjectSaveBatch.builder()
                .project(project)
                .user(null)
                .guestSession(guestSession)
                .status(status)
                .savedFileCount(savedFileCount)
                .build();

        return projectSaveBatchRepository.save(saveBatch);
    }
}