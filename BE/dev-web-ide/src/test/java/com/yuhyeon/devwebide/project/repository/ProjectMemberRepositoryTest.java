package com.yuhyeon.devwebide.project.repository;

import com.yuhyeon.devwebide.project.domain.Project;
import com.yuhyeon.devwebide.project.domain.ProjectMember;
import com.yuhyeon.devwebide.project.domain.ProjectMemberRole;
import com.yuhyeon.devwebide.project.domain.ProjectMemberStatus;
import com.yuhyeon.devwebide.project.domain.ProjectStatus;
import com.yuhyeon.devwebide.project.domain.ProjectType;
import com.yuhyeon.devwebide.project.domain.ProjectVisibility;
import com.yuhyeon.devwebide.runtime.domain.Runtime;
import com.yuhyeon.devwebide.runtime.domain.RuntimeLanguage;
import com.yuhyeon.devwebide.runtime.domain.RuntimeStatus;
import com.yuhyeon.devwebide.runtime.repository.RuntimeRepository;
import com.yuhyeon.devwebide.user.domain.User;
import com.yuhyeon.devwebide.user.domain.UserRole;
import com.yuhyeon.devwebide.user.domain.UserStatus;
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
class ProjectMemberRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RuntimeRepository runtimeRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Test
    @DisplayName("프로젝트 멤버 저장")
    void saveProjectMember() {
        User owner = saveUser("owner@test.com", "소유자");
        Runtime runtime = saveRuntime("node-20-save");
        Project project = saveTeamProject(owner, runtime, "팀 프로젝트");

        ProjectMember projectMember = ProjectMember.builder()
                .project(project)
                .user(owner)
                .role(ProjectMemberRole.OWNER)
                .status(ProjectMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();

        ProjectMember savedProjectMember =
                projectMemberRepository.save(projectMember);

        assertThat(savedProjectMember.getId()).isNotNull();
        assertThat(savedProjectMember.getProject().getId())
                .isEqualTo(project.getId());
        assertThat(savedProjectMember.getUser().getId())
                .isEqualTo(owner.getId());
        assertThat(savedProjectMember.getRole())
                .isEqualTo(ProjectMemberRole.OWNER);
        assertThat(savedProjectMember.getStatus())
                .isEqualTo(ProjectMemberStatus.ACTIVE);
        assertThat(savedProjectMember.getJoinedAt()).isNotNull();
    }

    @Test
    @DisplayName("프로젝트 ID와 사용자 ID로 멤버 조회")
    void findByProjectIdAndUserId() {
        User owner = saveUser("owner-find@test.com", "소유자");
        User member = saveUser("member-find@test.com", "멤버");
        Runtime runtime = saveRuntime("node-20-find");
        Project project = saveTeamProject(owner, runtime, "조회 팀 프로젝트");

        ProjectMember projectMember = saveProjectMember(
                project,
                member,
                ProjectMemberRole.EDITOR,
                ProjectMemberStatus.ACTIVE,
                owner,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
        );

        Optional<ProjectMember> result =
                projectMemberRepository.findByProjectIdAndUserId(
                        project.getId(),
                        member.getId()
                );

        assertThat(result).isPresent();
        assertThat(result.get().getId())
                .isEqualTo(projectMember.getId());
        assertThat(result.get().getRole())
                .isEqualTo(ProjectMemberRole.EDITOR);
        assertThat(result.get().getStatus())
                .isEqualTo(ProjectMemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("프로젝트 ID, 사용자 ID, 상태로 멤버 조회")
    void findByProjectIdAndUserIdAndStatus() {
        User owner = saveUser("owner-status@test.com", "소유자");
        User member = saveUser("member-status@test.com", "멤버");
        Runtime runtime = saveRuntime("node-20-status");
        Project project = saveTeamProject(owner, runtime, "상태 조회 팀 프로젝트");

        saveProjectMember(
                project,
                member,
                ProjectMemberRole.VIEWER,
                ProjectMemberStatus.INVITED,
                owner,
                LocalDateTime.now(),
                null
        );

        Optional<ProjectMember> result =
                projectMemberRepository.findByProjectIdAndUserIdAndStatus(
                        project.getId(),
                        member.getId(),
                        ProjectMemberStatus.INVITED
                );

        Optional<ProjectMember> notMatched =
                projectMemberRepository.findByProjectIdAndUserIdAndStatus(
                        project.getId(),
                        member.getId(),
                        ProjectMemberStatus.ACTIVE
                );

        assertThat(result).isPresent();
        assertThat(result.get().getStatus())
                .isEqualTo(ProjectMemberStatus.INVITED);
        assertThat(notMatched).isEmpty();
    }

    @Test
    @DisplayName("프로젝트 ID와 사용자 ID로 멤버 존재 여부 확인")
    void existsByProjectIdAndUserId() {
        User owner = saveUser("owner-exists@test.com", "소유자");
        User member = saveUser("member-exists@test.com", "멤버");
        User otherUser = saveUser("other-exists@test.com", "다른사용자");
        Runtime runtime = saveRuntime("node-20-exists");
        Project project = saveTeamProject(owner, runtime, "존재 확인 팀 프로젝트");

        saveProjectMember(
                project,
                member,
                ProjectMemberRole.EDITOR,
                ProjectMemberStatus.ACTIVE,
                owner,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
        );

        boolean exists =
                projectMemberRepository.existsByProjectIdAndUserId(
                        project.getId(),
                        member.getId()
                );

        boolean notExists =
                projectMemberRepository.existsByProjectIdAndUserId(
                        project.getId(),
                        otherUser.getId()
                );

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("프로젝트의 특정 상태 멤버 목록 조회")
    void findByProjectIdAndStatusOrderByJoinedAtDesc() {
        User owner = saveUser("owner-list@test.com", "소유자");
        User firstMember = saveUser("first-list@test.com", "첫번째멤버");
        User secondMember = saveUser("second-list@test.com", "두번째멤버");
        User invitedMember = saveUser("invited-list@test.com", "초대멤버");

        Runtime runtime = saveRuntime("node-20-list");
        Project project = saveTeamProject(owner, runtime, "멤버 목록 팀 프로젝트");

        LocalDateTime now = LocalDateTime.now();

        ProjectMember first = saveProjectMember(
                project,
                firstMember,
                ProjectMemberRole.EDITOR,
                ProjectMemberStatus.ACTIVE,
                owner,
                now.minusDays(3),
                now.minusDays(2)
        );

        ProjectMember second = saveProjectMember(
                project,
                secondMember,
                ProjectMemberRole.VIEWER,
                ProjectMemberStatus.ACTIVE,
                owner,
                now.minusDays(2),
                now
        );

        saveProjectMember(
                project,
                invitedMember,
                ProjectMemberRole.VIEWER,
                ProjectMemberStatus.INVITED,
                owner,
                now.minusDays(1),
                null
        );

        List<ProjectMember> result =
                projectMemberRepository.findByProjectIdAndStatusOrderByJoinedAtDesc(
                        project.getId(),
                        ProjectMemberStatus.ACTIVE
                );

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(second.getId());
        assertThat(result.get(1).getId()).isEqualTo(first.getId());
        assertThat(result)
                .allMatch(ProjectMember::isActive);
    }

    @Test
    @DisplayName("사용자가 참여 중인 프로젝트 멤버 정보 목록 조회")
    void findByUserIdAndStatusOrderByJoinedAtDesc() {
        User owner = saveUser("owner-shared@test.com", "소유자");
        User member = saveUser("member-shared@test.com", "공유멤버");

        Runtime runtime = saveRuntime("node-20-shared");

        Project firstProject =
                saveTeamProject(owner, runtime, "첫 번째 공유 프로젝트");
        Project secondProject =
                saveTeamProject(owner, runtime, "두 번째 공유 프로젝트");

        LocalDateTime now = LocalDateTime.now();

        ProjectMember first = saveProjectMember(
                firstProject,
                member,
                ProjectMemberRole.EDITOR,
                ProjectMemberStatus.ACTIVE,
                owner,
                now.minusDays(4),
                now.minusDays(3)
        );

        ProjectMember second = saveProjectMember(
                secondProject,
                member,
                ProjectMemberRole.VIEWER,
                ProjectMemberStatus.ACTIVE,
                owner,
                now.minusDays(2),
                now
        );

        List<ProjectMember> result =
                projectMemberRepository.findByUserIdAndStatusOrderByJoinedAtDesc(
                        member.getId(),
                        ProjectMemberStatus.ACTIVE
                );

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(second.getId());
        assertThat(result.get(1).getId()).isEqualTo(first.getId());
        assertThat(result)
                .allMatch(projectMember ->
                        projectMember.getUser().getId().equals(member.getId())
                );
    }

    @Test
    @DisplayName("프로젝트의 특정 권한과 상태를 가진 멤버 목록 조회")
    void findByProjectIdAndRoleAndStatus() {
        User owner = saveUser("owner-role@test.com", "소유자");
        User editor = saveUser("editor-role@test.com", "에디터");
        User viewer = saveUser("viewer-role@test.com", "뷰어");

        Runtime runtime = saveRuntime("node-20-role");
        Project project = saveTeamProject(owner, runtime, "권한 조회 팀 프로젝트");

        saveProjectMember(
                project,
                editor,
                ProjectMemberRole.EDITOR,
                ProjectMemberStatus.ACTIVE,
                owner,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
        );

        saveProjectMember(
                project,
                viewer,
                ProjectMemberRole.VIEWER,
                ProjectMemberStatus.ACTIVE,
                owner,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
        );

        List<ProjectMember> result =
                projectMemberRepository.findByProjectIdAndRoleAndStatus(
                        project.getId(),
                        ProjectMemberRole.EDITOR,
                        ProjectMemberStatus.ACTIVE
                );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getId())
                .isEqualTo(editor.getId());
        assertThat(result.get(0).getRole())
                .isEqualTo(ProjectMemberRole.EDITOR);
    }

    @Test
    @DisplayName("프로젝트에 특정 권한과 상태를 가진 멤버 존재 여부 확인")
    void existsByProjectIdAndRoleAndStatus() {
        User owner = saveUser("owner-role-exists@test.com", "소유자");
        User member = saveUser("member-role-exists@test.com", "멤버");

        Runtime runtime = saveRuntime("node-20-role-exists");
        Project project = saveTeamProject(owner, runtime, "OWNER 존재 확인 프로젝트");

        saveProjectMember(
                project,
                owner,
                ProjectMemberRole.OWNER,
                ProjectMemberStatus.ACTIVE,
                null,
                null,
                LocalDateTime.now()
        );

        saveProjectMember(
                project,
                member,
                ProjectMemberRole.EDITOR,
                ProjectMemberStatus.ACTIVE,
                owner,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
        );

        boolean existsOwner =
                projectMemberRepository.existsByProjectIdAndRoleAndStatus(
                        project.getId(),
                        ProjectMemberRole.OWNER,
                        ProjectMemberStatus.ACTIVE
                );

        boolean existsMaintainer =
                projectMemberRepository.existsByProjectIdAndRoleAndStatus(
                        project.getId(),
                        ProjectMemberRole.MAINTAINER,
                        ProjectMemberStatus.ACTIVE
                );

        assertThat(existsOwner).isTrue();
        assertThat(existsMaintainer).isFalse();
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

    private Runtime saveRuntime(String name) {
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

    private Project saveTeamProject(
            User owner,
            Runtime runtime,
            String name
    ) {
        Project project = Project.builder()
                .ownerUser(owner)
                .runtime(runtime)
                .name(name)
                .description("팀 프로젝트 테스트")
                .projectType(ProjectType.TEAM)
                .visibility(ProjectVisibility.TEAM)
                .status(ProjectStatus.ACTIVE)
                .storagePath("/projects/test/" + name)
                .build();

        return projectRepository.save(project);
    }

    private ProjectMember saveProjectMember(
            Project project,
            User user,
            ProjectMemberRole role,
            ProjectMemberStatus status,
            User invitedByUser,
            LocalDateTime invitedAt,
            LocalDateTime joinedAt
    ) {
        ProjectMember projectMember = ProjectMember.builder()
                .project(project)
                .user(user)
                .role(role)
                .status(status)
                .invitedByUser(invitedByUser)
                .invitedAt(invitedAt)
                .joinedAt(joinedAt)
                .build();

        return projectMemberRepository.save(projectMember);
    }
}