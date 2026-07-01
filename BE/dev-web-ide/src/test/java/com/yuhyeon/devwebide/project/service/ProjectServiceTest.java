package com.yuhyeon.devwebide.project.service;

import com.yuhyeon.devwebide.project.domain.*;
import com.yuhyeon.devwebide.project.dto.*;
import com.yuhyeon.devwebide.project.repository.*;
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
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ProjectService 테스트
 *
 * 프로젝트 생성 시 Project, ProjectMember, ProjectSettings,
 * ProjectFile이 정상적으로 함께 생성되는지 검증합니다.
 */
@DataJpaTest
@Import(ProjectService.class)
class ProjectServiceTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private ProjectSettingsRepository projectSettingsRepository;

    @Autowired
    private ProjectFileRepository projectFileRepository;

    @Autowired
    private RuntimeRepository runtimeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GuestSessionRepository guestSessionRepository;
    @Autowired
    private ProjectAccessLogRepository projectAccessLogRepository;

    @Test
    @DisplayName("개인 프로젝트를 생성한다")
    void createPersonalProject() {
        // given
        User ownerUser = saveUser(
                "owner@example.com",
                "owner"
        );

        Runtime runtime = saveRuntime(
                "node-20",
                "Node.js 20"
        );

        ProjectCreateRequest request = new ProjectCreateRequest(
                "personal-project",
                "개인 프로젝트입니다.",
                runtime.getId(),
                ProjectType.PERSONAL,
                ProjectVisibility.PRIVATE,
                List.of()
        );

        // when
        ProjectCreateResponse response = projectService.createProject(
                request,
                ownerUser.getId(),
                null
        );

        // then
        Project project = projectRepository.findById(response.id())
                .orElseThrow();

        assertThat(project.getName()).isEqualTo("personal-project");
        assertThat(project.getOwnerUser().getId()).isEqualTo(ownerUser.getId());
        assertThat(project.getGuestSession()).isNull();
        assertThat(project.getRuntime().getId()).isEqualTo(runtime.getId());
        assertThat(project.getProjectType()).isEqualTo(ProjectType.PERSONAL);
        assertThat(project.getVisibility()).isEqualTo(ProjectVisibility.PRIVATE);

        assertThat(projectSettingsRepository.existsByProjectId(project.getId()))
                .isTrue();

        ProjectFile rootDirectory = projectFileRepository
                .findByProjectIdAndParentFileIsNullAndStatus(
                        project.getId(),
                        ProjectFileStatus.ACTIVE
                )
                .orElseThrow();

        assertThat(rootDirectory.isRootDirectory()).isTrue();
    }

    @Test
    @DisplayName("팀 프로젝트를 생성하면 OWNER 멤버와 초대 멤버가 생성된다")
    void createTeamProject() {
        // given
        User ownerUser = saveUser(
                "owner@example.com",
                "owner"
        );

        User memberUser1 = saveUser(
                "member1@example.com",
                "member1"
        );

        User memberUser2 = saveUser(
                "member2@example.com",
                "member2"
        );

        Runtime runtime = saveRuntime(
                "java-21",
                "Java 21"
        );

        ProjectCreateRequest request = new ProjectCreateRequest(
                "team-project",
                "팀 프로젝트입니다.",
                runtime.getId(),
                ProjectType.TEAM,
                ProjectVisibility.TEAM,
                List.of(memberUser1.getId(), memberUser2.getId())
        );

        // when
        ProjectCreateResponse response = projectService.createProject(
                request,
                ownerUser.getId(),
                null
        );

        // then
        Project project = projectRepository.findById(response.id())
                .orElseThrow();

        assertThat(project.getProjectType()).isEqualTo(ProjectType.TEAM);
        assertThat(project.getVisibility()).isEqualTo(ProjectVisibility.TEAM);

        boolean existsOwner = projectMemberRepository
                .existsByProjectIdAndRoleAndStatus(
                        project.getId(),
                        ProjectMemberRole.OWNER,
                        ProjectMemberStatus.ACTIVE
                );

        assertThat(existsOwner).isTrue();

        List<ProjectMember> invitedMembers = projectMemberRepository
                .findByProjectIdAndStatusOrderByJoinedAtDesc(
                        project.getId(),
                        ProjectMemberStatus.INVITED
                );

        assertThat(invitedMembers).hasSize(2);
        assertThat(invitedMembers)
                .extracting(projectMember -> projectMember.getUser().getId())
                .containsExactlyInAnyOrder(
                        memberUser1.getId(),
                        memberUser2.getId()
                );

        assertThat(projectSettingsRepository.existsByProjectId(project.getId()))
                .isTrue();

        assertThat(projectFileRepository.findByProjectIdAndParentFileIsNullAndStatus(
                project.getId(),
                ProjectFileStatus.ACTIVE
        )).isPresent();
    }

    @Test
    @DisplayName("게스트 프로젝트를 생성한다")
    void createGuestProject() {
        // given
        GuestSession guestSession = saveGuestSession(
                "guest-token"
        );

        Runtime runtime = saveRuntime(
                "python-3.12",
                "Python 3.12"
        );

        ProjectCreateRequest request = new ProjectCreateRequest(
                "guest-project",
                "게스트 프로젝트입니다.",
                runtime.getId(),
                ProjectType.GUEST,
                ProjectVisibility.PRIVATE,
                List.of()
        );

        // when
        ProjectCreateResponse response = projectService.createProject(
                request,
                null,
                guestSession.getId()
        );

        // then
        Project project = projectRepository.findById(response.id())
                .orElseThrow();

        assertThat(project.getOwnerUser()).isNull();
        assertThat(project.getGuestSession().getId()).isEqualTo(guestSession.getId());
        assertThat(project.getProjectType()).isEqualTo(ProjectType.GUEST);
        assertThat(project.getVisibility()).isEqualTo(ProjectVisibility.PRIVATE);

        assertThat(projectSettingsRepository.existsByProjectId(project.getId()))
                .isTrue();

        assertThat(projectFileRepository.findByProjectIdAndParentFileIsNullAndStatus(
                project.getId(),
                ProjectFileStatus.ACTIVE
        )).isPresent();
    }

    @Test
    @DisplayName("존재하지 않는 런타임 ID로 프로젝트를 생성하면 예외가 발생한다")
    void createProjectWithInvalidRuntimeId() {
        // given
        User ownerUser = saveUser(
                "owner@example.com",
                "owner"
        );

        ProjectCreateRequest request = new ProjectCreateRequest(
                "invalid-runtime-project",
                "존재하지 않는 런타임 테스트입니다.",
                999999L,
                ProjectType.PERSONAL,
                ProjectVisibility.PRIVATE,
                List.of()
        );

        // when & then
        assertThatThrownBy(() -> projectService.createProject(
                request,
                ownerUser.getId(),
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 런타임입니다.");
    }

    @Test
    @DisplayName("팀 프로젝트의 공개 범위가 TEAM이 아니면 예외가 발생한다")
    void createTeamProjectWithInvalidVisibility() {
        // given
        User ownerUser = saveUser(
                "owner@example.com",
                "owner"
        );

        Runtime runtime = saveRuntime(
                "node-20",
                "Node.js 20"
        );

        ProjectCreateRequest request = new ProjectCreateRequest(
                "invalid-team-project",
                "잘못된 팀 프로젝트 공개 범위 테스트입니다.",
                runtime.getId(),
                ProjectType.TEAM,
                ProjectVisibility.PRIVATE,
                List.of()
        );

        // when & then
        assertThatThrownBy(() -> projectService.createProject(
                request,
                ownerUser.getId(),
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("팀 프로젝트의 공개 범위는 TEAM이어야 합니다.");
    }

    @Test
    @DisplayName("내 프로젝트 목록을 조회한다")
    void getMyProjects() {
        // given
        User ownerUser = saveUser(
                "my-project-owner@example.com",
                "owner"
        );

        Runtime runtime = saveRuntime(
                "node-my-projects",
                "Node.js My Projects"
        );

        Project firstProject = savePersonalProject(
                ownerUser,
                runtime,
                "first-project",
                "첫 번째 프로젝트입니다.",
                "/projects/my-first"
        );

        Project secondProject = savePersonalProject(
                ownerUser,
                runtime,
                "second-project",
                "두 번째 프로젝트입니다.",
                "/projects/my-second"
        );

        // when
        List<ProjectSummaryResponse> result =
                projectService.getMyProjects(ownerUser.getId());

        // then
        assertThat(result).hasSize(2);

        assertThat(result)
                .extracting(ProjectSummaryResponse::id)
                .containsExactly(
                        secondProject.getId(),
                        firstProject.getId()
                );

        assertThat(result.get(0).name()).isEqualTo("second-project");
        assertThat(result.get(0).description()).isEqualTo("두 번째 프로젝트입니다.");
        assertThat(result.get(0).projectType()).isEqualTo(ProjectType.PERSONAL);
        assertThat(result.get(0).visibility()).isEqualTo(ProjectVisibility.PRIVATE);
        assertThat(result.get(0).status()).isEqualTo(ProjectStatus.ACTIVE);

        assertThat(result.get(0).runtimeId()).isEqualTo(runtime.getId());
        assertThat(result.get(0).runtimeName()).isEqualTo(runtime.getName());
        assertThat(result.get(0).runtimeDisplayName()).isEqualTo(runtime.getDisplayName());
        assertThat(result.get(0).runtimeLanguage()).isEqualTo(runtime.getLanguage());
    }

    @Test
    @DisplayName("내 프로젝트 목록 조회 시 ACTIVE 프로젝트만 조회한다")
    void getMyProjectsOnlyActiveProjects() {
        // given
        User ownerUser = saveUser(
                "active-owner@example.com",
                "activeOwner"
        );

        Runtime runtime = saveRuntime(
                "python-my-projects",
                "Python My Projects"
        );

        Project activeProject = savePersonalProject(
                ownerUser,
                runtime,
                "active-project",
                "활성 프로젝트입니다.",
                "/projects/active"
        );

        Project deletedProject = savePersonalProject(
                ownerUser,
                runtime,
                "deleted-project",
                "삭제된 프로젝트입니다.",
                "/projects/deleted"
        );

        deletedProject.delete();
        projectRepository.save(deletedProject);

        // when
        List<ProjectSummaryResponse> result =
                projectService.getMyProjects(ownerUser.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(activeProject.getId());
        assertThat(result.get(0).name()).isEqualTo("active-project");
        assertThat(result.get(0).status()).isEqualTo(ProjectStatus.ACTIVE);
    }

    @Test
    @DisplayName("내 프로젝트 목록 조회 시 다른 사용자의 프로젝트는 조회하지 않는다")
    void getMyProjectsExcludeOtherUserProjects() {
        // given
        User ownerUser = saveUser(
                "owner-only@example.com",
                "ownerOnly"
        );

        User otherUser = saveUser(
                "other-owner@example.com",
                "otherOwner"
        );

        Runtime runtime = saveRuntime(
                "java-my-projects",
                "Java My Projects"
        );

        Project myProject = savePersonalProject(
                ownerUser,
                runtime,
                "my-project",
                "내 프로젝트입니다.",
                "/projects/my-project"
        );

        savePersonalProject(
                otherUser,
                runtime,
                "other-project",
                "다른 사용자 프로젝트입니다.",
                "/projects/other-project"
        );

        // when
        List<ProjectSummaryResponse> result =
                projectService.getMyProjects(ownerUser.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(myProject.getId());
        assertThat(result.get(0).name()).isEqualTo("my-project");
    }

    /**
     * 테스트용 사용자 저장
     *
     * @param email 이메일
     * @param nickname 닉네임
     * @return 저장된 사용자
     */
    private User saveUser(
            String email,
            String nickname
    ) {
        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        return userRepository.save(user);
    }

    /**
     * 테스트용 런타임 저장
     *
     * @param name 런타임 내부 이름
     * @param displayName 런타임 표시 이름
     * @return 저장된 런타임
     */
    private Runtime saveRuntime(
            String name,
            String displayName
    ) {
        Runtime runtime = Runtime.builder()
                .name(name)
                .displayName(displayName)
                .version("1.0")
                .dockerImage("test-image")
                .language(RuntimeLanguage.values()[0])
                .status(RuntimeStatus.ACTIVE)
                .build();

        return runtimeRepository.save(runtime);
    }

    /**
     * 테스트용 게스트 세션 저장
     *
     * @param guestToken 게스트 토큰
     * @return 저장된 게스트 세션
     */
    private GuestSession saveGuestSession(String guestToken) {
        GuestSession guestSession = GuestSession.builder()
                .guestToken(guestToken)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        return guestSessionRepository.save(guestSession);
    }

    /**
     * 테스트용 개인 프로젝트 저장
     *
     * @param ownerUser 프로젝트 소유자
     * @param runtime 프로젝트 런타임
     * @param name 프로젝트 이름
     * @param description 프로젝트 설명
     * @param storagePath 프로젝트 저장 경로
     * @return 저장된 프로젝트
     */
    private Project savePersonalProject(
            User ownerUser,
            Runtime runtime,
            String name,
            String description,
            String storagePath
    ) {
        Project project = Project.builder()
                .ownerUser(ownerUser)
                .guestSession(null)
                .runtime(runtime)
                .name(name)
                .description(description)
                .projectType(ProjectType.PERSONAL)
                .visibility(ProjectVisibility.PRIVATE)
                .status(ProjectStatus.ACTIVE)
                .storagePath(storagePath)
                .build();

        return projectRepository.save(project);
    }

    @Test
    @DisplayName("프로젝트 상세 조회")
    void getProjectDetail() {
        // given
        User owner = userRepository.save(createUser(
                "owner-detail@test.com",
                "프로젝트소유자"
        ));

        User member = userRepository.save(createUser(
                "member-detail@test.com",
                "팀멤버"
        ));

        Runtime runtime = runtimeRepository.save(createRuntime());

        Project project = Project.builder()
                .ownerUser(owner)
                .guestSession(null)
                .runtime(runtime)
                .name("web-ide-project")
                .description("웹 IDE 프로젝트")
                .projectType(ProjectType.TEAM)
                .visibility(ProjectVisibility.TEAM)
                .status(ProjectStatus.ACTIVE)
                .storagePath("/projects/detail")
                .build();

        Project savedProject = projectRepository.save(project);

        ProjectSettings projectSettings = ProjectSettings.builder()
                .project(savedProject)
                .autoSaveEnabled(true)
                .formatOnSaveEnabled(true)
                .guestCanEdit(false)
                .shareCursorPosition(true)
                .build();

        projectSettingsRepository.save(projectSettings);

        ProjectMember projectMember = ProjectMember.builder()
                .project(savedProject)
                .user(member)
                .role(ProjectMemberRole.EDITOR)
                .status(ProjectMemberStatus.ACTIVE)
                .invitedByUser(owner)
                .invitedAt(LocalDateTime.now().minusDays(1))
                .joinedAt(LocalDateTime.now())
                .build();

        projectMemberRepository.save(projectMember);

        // when
        ProjectDetailResponse response =
                projectService.getProjectDetail(savedProject.getId());

        // then
        assertThat(response.id()).isEqualTo(savedProject.getId());
        assertThat(response.name()).isEqualTo("web-ide-project");
        assertThat(response.description()).isEqualTo("웹 IDE 프로젝트");
        assertThat(response.projectType()).isEqualTo(ProjectType.TEAM);
        assertThat(response.visibility()).isEqualTo(ProjectVisibility.TEAM);
        assertThat(response.status()).isEqualTo(ProjectStatus.ACTIVE);
        assertThat(response.storagePath()).isEqualTo("/projects/detail");

        assertThat(response.runtime().id()).isEqualTo(runtime.getId());
        assertThat(response.runtime().name()).isEqualTo("node-20");
        assertThat(response.runtime().displayName()).isEqualTo("Node.js 20");
        assertThat(response.runtime().language()).isEqualTo(RuntimeLanguage.NODE);

        assertThat(response.settings().autoSaveEnabled()).isTrue();
        assertThat(response.settings().formatOnSaveEnabled()).isTrue();
        assertThat(response.settings().guestCanEdit()).isFalse();
        assertThat(response.settings().shareCursorPosition()).isTrue();

        assertThat(response.members()).hasSize(1);
        assertThat(response.members().get(0).userId()).isEqualTo(member.getId());
        assertThat(response.members().get(0).nickname()).isEqualTo("팀멤버");
        assertThat(response.members().get(0).role()).isEqualTo(ProjectMemberRole.EDITOR);
        assertThat(response.members().get(0).status()).isEqualTo(ProjectMemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트 상세 조회 시 예외 발생")
    void getProjectDetailNotFound() {
        // given
        Long notExistsProjectId = 999999L;

        // when & then
        assertThatThrownBy(() -> projectService.getProjectDetail(notExistsProjectId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않거나 삭제된 프로젝트입니다.");
    }

    @Test
    @DisplayName("프로젝트 설정이 없으면 상세 조회 시 예외 발생")
    void getProjectDetailWithoutSettings() {
        // given
        User owner = userRepository.save(createUser(
                "owner-no-settings@test.com",
                "설정없는소유자"
        ));

        Runtime runtime = runtimeRepository.save(Runtime.builder()
                .name("python-3.12")
                .displayName("Python 3.12")
                .version("3.12")
                .dockerImage("python:3.12")
                .language(RuntimeLanguage.PYTHON)
                .status(RuntimeStatus.ACTIVE)
                .build());

        Project project = Project.builder()
                .ownerUser(owner)
                .guestSession(null)
                .runtime(runtime)
                .name("settings-missing-project")
                .description("설정 없는 프로젝트")
                .projectType(ProjectType.PERSONAL)
                .visibility(ProjectVisibility.PRIVATE)
                .status(ProjectStatus.ACTIVE)
                .storagePath("/projects/no-settings")
                .build();

        Project savedProject = projectRepository.save(project);

        // when & then
        assertThatThrownBy(() -> projectService.getProjectDetail(savedProject.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("프로젝트 설정이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("회원 사용자는 프로젝트를 열 수 있다")
    void openProjectByUser() {
        // given
        User user = userRepository.save(createUser());
        Runtime runtime = runtimeRepository.save(createRuntime());
        Project project = projectRepository.save(createUserProject(user, runtime));

        // when
        ProjectOpenResponse response = projectService.openProject(
                project.getId(),
                user.getId(),
                null
        );

        // then
        assertThat(response.projectId()).isEqualTo(project.getId());
        assertThat(response.name()).isEqualTo(project.getName());
        assertThat(response.runtimeId()).isEqualTo(runtime.getId());
        assertThat(response.openedAt()).isNotNull();

        List<ProjectAccessLog> accessLogs =
                projectAccessLogRepository.findByUserIdAndAccessTypeOrderByAccessedAtDesc(
                        user.getId(),
                        ProjectAccessType.OPEN
                );

        assertThat(accessLogs).hasSize(1);
        assertThat(accessLogs.get(0).getProject().getId()).isEqualTo(project.getId());
        assertThat(accessLogs.get(0).getUser().getId()).isEqualTo(user.getId());
        assertThat(accessLogs.get(0).getGuestSession()).isNull();
        assertThat(accessLogs.get(0).getAccessType()).isEqualTo(ProjectAccessType.OPEN);
    }

    @Test
    @DisplayName("게스트 사용자는 프로젝트를 열 수 있다")
    void openProjectByGuestSession() {
        // given
        GuestSession guestSession = guestSessionRepository.save(createGuestSession());
        Runtime runtime = runtimeRepository.save(createRuntime());
        Project project = projectRepository.save(createGuestProject(guestSession, runtime));

        // when
        ProjectOpenResponse response = projectService.openProject(
                project.getId(),
                null,
                guestSession.getId()
        );

        // then
        assertThat(response.projectId()).isEqualTo(project.getId());
        assertThat(response.name()).isEqualTo(project.getName());
        assertThat(response.runtimeId()).isEqualTo(runtime.getId());
        assertThat(response.openedAt()).isNotNull();

        List<ProjectAccessLog> accessLogs =
                projectAccessLogRepository.findByGuestSessionIdAndAccessTypeOrderByAccessedAtDesc(
                        guestSession.getId(),
                        ProjectAccessType.OPEN
                );

        assertThat(accessLogs).hasSize(1);
        assertThat(accessLogs.get(0).getProject().getId()).isEqualTo(project.getId());
        assertThat(accessLogs.get(0).getUser()).isNull();
        assertThat(accessLogs.get(0).getGuestSession().getId()).isEqualTo(guestSession.getId());
        assertThat(accessLogs.get(0).getAccessType()).isEqualTo(ProjectAccessType.OPEN);
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트를 열면 예외가 발생한다")
    void openProjectWithNotFoundProject() {
        // given
        User user = userRepository.save(createUser());
        Long notFoundProjectId = 999999L;

        // when & then
        assertThatThrownBy(() -> projectService.openProject(
                notFoundProjectId,
                user.getId(),
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("프로젝트를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("삭제된 프로젝트는 열 수 없다")
    void openDeletedProject() {
        // given
        User user = userRepository.save(createUser());
        Runtime runtime = runtimeRepository.save(createRuntime());
        Project deletedProject = projectRepository.save(createDeletedUserProject(user, runtime));

        // when & then
        assertThatThrownBy(() -> projectService.openProject(
                deletedProject.getId(),
                user.getId(),
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("프로젝트를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("userId와 guestSessionId가 둘 다 없으면 예외가 발생한다")
    void openProjectWithoutRequester() {
        // given
        User user = userRepository.save(createUser());
        Runtime runtime = runtimeRepository.save(createRuntime());
        Project project = projectRepository.save(createUserProject(user, runtime));

        // when & then
        assertThatThrownBy(() -> projectService.openProject(
                project.getId(),
                null,
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 사용자 또는 게스트 세션 중 하나만 지정해야 합니다.");
    }

    @Test
    @DisplayName("userId와 guestSessionId가 둘 다 있으면 예외가 발생한다")
    void openProjectWithUserAndGuestSession() {
        // given
        User user = userRepository.save(createUser());
        GuestSession guestSession = guestSessionRepository.save(createGuestSession());
        Runtime runtime = runtimeRepository.save(createRuntime());
        Project project = projectRepository.save(createUserProject(user, runtime));

        // when & then
        assertThatThrownBy(() -> projectService.openProject(
                project.getId(),
                user.getId(),
                guestSession.getId()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 사용자 또는 게스트 세션 중 하나만 지정해야 합니다.");
    }

    @Test
    @DisplayName("존재하지 않는 회원 사용자가 프로젝트를 열면 예외가 발생한다")
    void openProjectWithNotFoundUser() {
        // given
        User user = userRepository.save(createUser());
        Runtime runtime = runtimeRepository.save(createRuntime());
        Project project = projectRepository.save(createUserProject(user, runtime));

        Long notFoundUserId = 999999L;

        // when & then
        assertThatThrownBy(() -> projectService.openProject(
                project.getId(),
                notFoundUserId,
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 게스트 세션으로 프로젝트를 열면 예외가 발생한다")
    void openProjectWithNotFoundGuestSession() {
        // given
        GuestSession guestSession = guestSessionRepository.save(createGuestSession());
        Runtime runtime = runtimeRepository.save(createRuntime());
        Project project = projectRepository.save(createGuestProject(guestSession, runtime));

        Long notFoundGuestSessionId = 999999L;

        // when & then
        assertThatThrownBy(() -> projectService.openProject(
                project.getId(),
                null,
                notFoundGuestSessionId
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("게스트 세션을 찾을 수 없습니다.");
    }

    private User createUser(String email, String nickname) {
        return User.builder()
                .email(email)
                .nickname(nickname)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    private Runtime createRuntime() {
        return Runtime.builder()
                .name("node-20")
                .displayName("Node.js 20")
                .version("20")
                .dockerImage("node:20")
                .language(RuntimeLanguage.NODE)
                .status(RuntimeStatus.ACTIVE)
                .build();
    }

    private User createUser() {
        return User.builder()
                .email("test-" + UUID.randomUUID() + "@example.com")
                .nickname("테스터")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    private GuestSession createGuestSession() {
        return GuestSession.builder()
                .guestToken("guest-token-" + UUID.randomUUID())
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();
    }

    private Project createUserProject(User user, Runtime runtime) {
        return Project.builder()
                .ownerUser(user)
                .guestSession(null)
                .runtime(runtime)
                .name("회원 프로젝트")
                .description("회원 프로젝트 설명")
                .projectType(ProjectType.PERSONAL)
                .visibility(ProjectVisibility.PRIVATE)
                .status(ProjectStatus.ACTIVE)
                .storagePath("/projects/" + UUID.randomUUID())
                .build();
    }

    private Project createGuestProject(GuestSession guestSession, Runtime runtime) {
        return Project.builder()
                .ownerUser(null)
                .guestSession(guestSession)
                .runtime(runtime)
                .name("게스트 프로젝트")
                .description("게스트 프로젝트 설명")
                .projectType(ProjectType.GUEST)
                .visibility(ProjectVisibility.PRIVATE)
                .status(ProjectStatus.ACTIVE)
                .storagePath("/projects/" + UUID.randomUUID())
                .build();
    }

    private Project createDeletedUserProject(User user, Runtime runtime) {
        return Project.builder()
                .ownerUser(user)
                .guestSession(null)
                .runtime(runtime)
                .name("삭제된 프로젝트")
                .description("삭제된 프로젝트 설명")
                .projectType(ProjectType.PERSONAL)
                .visibility(ProjectVisibility.PRIVATE)
                .status(ProjectStatus.DELETED)
                .storagePath("/projects/" + UUID.randomUUID())
                .build();
    }
}