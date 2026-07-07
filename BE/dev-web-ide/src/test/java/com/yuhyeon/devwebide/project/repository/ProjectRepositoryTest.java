package com.yuhyeon.devwebide.project.repository;

import com.yuhyeon.devwebide.project.domain.Project;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GuestSessionRepository guestSessionRepository;

    @Autowired
    private RuntimeRepository runtimeRepository;

    @Test
    @DisplayName("회원 프로젝트 저장")
    void saveUserProject() {
        User user = saveUser("project@test.com", "프로젝트테스터");
        Runtime runtime = saveRuntime("node-20", "Node.js 20");

        Project project = Project.builder()
                .ownerUser(user)
                .runtime(runtime)
                .name("회원 프로젝트")
                .description("회원이 생성한 프로젝트")
                .projectType(ProjectType.PERSONAL)
                .visibility(ProjectVisibility.PRIVATE)
                .status(ProjectStatus.ACTIVE)
                .storagePath("/projects/user-project")
                .build();

        Project savedProject = projectRepository.save(project);

        assertThat(savedProject.getId()).isNotNull();
        assertThat(savedProject.getOwnerUser().getId()).isEqualTo(user.getId());
        assertThat(savedProject.getGuestSession()).isNull();
        assertThat(savedProject.getRuntime().getId()).isEqualTo(runtime.getId());
        assertThat(savedProject.getName()).isEqualTo("회원 프로젝트");
        assertThat(savedProject.getProjectType()).isEqualTo(ProjectType.PERSONAL);
        assertThat(savedProject.getVisibility()).isEqualTo(ProjectVisibility.PRIVATE);
        assertThat(savedProject.getStatus()).isEqualTo(ProjectStatus.ACTIVE);
        assertThat(savedProject.getStoragePath()).isEqualTo("/projects/user-project");
        assertThat(savedProject.getCreatedAt()).isNotNull();
        assertThat(savedProject.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("게스트 프로젝트 저장")
    void saveGuestProject() {
        GuestSession guestSession = saveGuestSession("guest-token-project");
        Runtime runtime = saveRuntime("python-3.12", "Python 3.12");

        Project project = Project.builder()
                .guestSession(guestSession)
                .runtime(runtime)
                .name("게스트 프로젝트")
                .description("게스트가 생성한 프로젝트")
                .projectType(ProjectType.GUEST)
                .visibility(ProjectVisibility.PRIVATE)
                .status(ProjectStatus.ACTIVE)
                .storagePath("/projects/guest-project")
                .build();

        Project savedProject = projectRepository.save(project);

        assertThat(savedProject.getId()).isNotNull();
        assertThat(savedProject.getOwnerUser()).isNull();
        assertThat(savedProject.getGuestSession().getId()).isEqualTo(guestSession.getId());
        assertThat(savedProject.getRuntime().getId()).isEqualTo(runtime.getId());
        assertThat(savedProject.getName()).isEqualTo("게스트 프로젝트");
        assertThat(savedProject.getProjectType()).isEqualTo(ProjectType.GUEST);
        assertThat(savedProject.getStatus()).isEqualTo(ProjectStatus.ACTIVE);
    }

    @Test
    @DisplayName("회원 소유 프로젝트 목록 조회")
    void findByOwnerUserIdAndStatusOrderByUpdatedAtDescIdDesc() {
        User user = saveUser("owner@test.com", "소유자");
        Runtime runtime = saveRuntime("java-21", "Java 21");

        Project firstProject = Project.builder()
                .ownerUser(user)
                .runtime(runtime)
                .name("첫 번째 프로젝트")
                .description("첫 번째")
                .projectType(ProjectType.PERSONAL)
                .visibility(ProjectVisibility.PRIVATE)
                .status(ProjectStatus.ACTIVE)
                .storagePath("/projects/first")
                .build();

        Project secondProject = Project.builder()
                .ownerUser(user)
                .runtime(runtime)
                .name("두 번째 프로젝트")
                .description("두 번째")
                .projectType(ProjectType.PERSONAL)
                .visibility(ProjectVisibility.PRIVATE)
                .status(ProjectStatus.ACTIVE)
                .storagePath("/projects/second")
                .build();

        projectRepository.save(firstProject);
        projectRepository.save(secondProject);

        List<Project> result =
                projectRepository.findByOwnerUserIdAndStatusOrderByUpdatedAtDescIdDesc(
                        user.getId(),
                        ProjectStatus.ACTIVE
                );

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Project::getName)
                .containsExactly("두 번째 프로젝트", "첫 번째 프로젝트");
    }

    @Test
    @DisplayName("게스트 세션 기준 프로젝트 목록 조회")
    void findByGuestSessionIdAndStatusOrderByUpdatedAtDesc() {
        GuestSession guestSession = saveGuestSession("guest-token-list");
        Runtime runtime = saveRuntime("cpp-gcc", "C++ GCC");

        Project project = Project.builder()
                .guestSession(guestSession)
                .runtime(runtime)
                .name("게스트 목록 프로젝트")
                .description("게스트 프로젝트 목록 조회")
                .projectType(ProjectType.GUEST)
                .visibility(ProjectVisibility.PRIVATE)
                .status(ProjectStatus.ACTIVE)
                .storagePath("/projects/guest-list")
                .build();

        projectRepository.save(project);

        List<Project> result =
                projectRepository.findByGuestSessionIdAndStatusOrderByUpdatedAtDesc(
                        guestSession.getId(),
                        ProjectStatus.ACTIVE
                );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("게스트 목록 프로젝트");
        assertThat(result.get(0).getGuestSession().getId())
                .isEqualTo(guestSession.getId());
    }

    @Test
    @DisplayName("프로젝트 ID와 ACTIVE 상태로 프로젝트 조회")
    void findByIdAndStatus() {
        User user = saveUser("active@test.com", "활성테스터");
        Runtime runtime = saveRuntime("node-18", "Node.js 18");

        Project project = Project.builder()
                .ownerUser(user)
                .runtime(runtime)
                .name("활성 프로젝트")
                .description("ACTIVE 상태 프로젝트")
                .projectType(ProjectType.PERSONAL)
                .visibility(ProjectVisibility.PRIVATE)
                .status(ProjectStatus.ACTIVE)
                .storagePath("/projects/active")
                .build();

        Project savedProject = projectRepository.save(project);

        Optional<Project> result =
                projectRepository.findByIdAndStatus(
                        savedProject.getId(),
                        ProjectStatus.ACTIVE
                );

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("활성 프로젝트");
        assertThat(result.get().getStatus()).isEqualTo(ProjectStatus.ACTIVE);
    }

    @Test
    @DisplayName("삭제 상태 프로젝트는 ACTIVE 상태 조회에서 조회되지 않음")
    void findByIdAndStatusDeletedProject() {
        User user = saveUser("deleted@test.com", "삭제테스터");
        Runtime runtime = saveRuntime("python-3.11", "Python 3.11");

        Project project = Project.builder()
                .ownerUser(user)
                .runtime(runtime)
                .name("삭제 프로젝트")
                .description("DELETED 상태 프로젝트")
                .projectType(ProjectType.PERSONAL)
                .visibility(ProjectVisibility.PRIVATE)
                .status(ProjectStatus.DELETED)
                .storagePath("/projects/deleted")
                .build();

        Project savedProject = projectRepository.save(project);

        Optional<Project> result =
                projectRepository.findByIdAndStatus(
                        savedProject.getId(),
                        ProjectStatus.ACTIVE
                );

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("회원 소유 프로젝트 이름 검색")
    void findByOwnerUserIdAndStatusAndNameContainingIgnoreCaseOrderByUpdatedAtDesc() {
        User user = saveUser("search@test.com", "검색테스터");
        Runtime runtime = saveRuntime("node-22", "Node.js 22");

        Project reactProject = Project.builder()
                .ownerUser(user)
                .runtime(runtime)
                .name("React Project")
                .description("React 프로젝트")
                .projectType(ProjectType.PERSONAL)
                .visibility(ProjectVisibility.PRIVATE)
                .status(ProjectStatus.ACTIVE)
                .storagePath("/projects/react")
                .build();

        Project springProject = Project.builder()
                .ownerUser(user)
                .runtime(runtime)
                .name("Spring Project")
                .description("Spring 프로젝트")
                .projectType(ProjectType.PERSONAL)
                .visibility(ProjectVisibility.PRIVATE)
                .status(ProjectStatus.ACTIVE)
                .storagePath("/projects/spring")
                .build();

        projectRepository.save(reactProject);
        projectRepository.save(springProject);

        List<Project> result =
                projectRepository
                        .findByOwnerUserIdAndStatusAndNameContainingIgnoreCaseOrderByUpdatedAtDesc(
                                user.getId(),
                                ProjectStatus.ACTIVE,
                                "react"
                        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("React Project");
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
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        return guestSessionRepository.save(guestSession);
    }

    private Runtime saveRuntime(String name, String displayName) {
        Runtime runtime = Runtime.builder()
                .name(name)
                .displayName(displayName)
                .version("1.0")
                .dockerImage(name + ":latest")
                .language(RuntimeLanguage.NODE)
                .status(RuntimeStatus.ACTIVE)
                .build();

        return runtimeRepository.save(runtime);
    }
}
