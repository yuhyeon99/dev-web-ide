package com.yuhyeon.devwebide.project.service;

import com.yuhyeon.devwebide.project.domain.Project;
import com.yuhyeon.devwebide.project.domain.ProjectFile;
import com.yuhyeon.devwebide.project.domain.ProjectFileStatus;
import com.yuhyeon.devwebide.project.domain.ProjectMember;
import com.yuhyeon.devwebide.project.domain.ProjectMemberRole;
import com.yuhyeon.devwebide.project.domain.ProjectMemberStatus;
import com.yuhyeon.devwebide.project.domain.ProjectType;
import com.yuhyeon.devwebide.project.domain.ProjectVisibility;
import com.yuhyeon.devwebide.project.dto.ProjectCreateRequest;
import com.yuhyeon.devwebide.project.dto.ProjectCreateResponse;
import com.yuhyeon.devwebide.project.repository.ProjectFileRepository;
import com.yuhyeon.devwebide.project.repository.ProjectMemberRepository;
import com.yuhyeon.devwebide.project.repository.ProjectRepository;
import com.yuhyeon.devwebide.project.repository.ProjectSettingsRepository;
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
}