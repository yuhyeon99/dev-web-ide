package com.yuhyeon.devwebide.project.service;

import com.yuhyeon.devwebide.project.domain.*;
import com.yuhyeon.devwebide.project.dto.ProjectMemberInviteRequest;
import com.yuhyeon.devwebide.project.dto.ProjectMemberManageResponse;
import com.yuhyeon.devwebide.project.dto.ProjectMemberRoleUpdateRequest;
import com.yuhyeon.devwebide.project.repository.ProjectMemberRepository;
import com.yuhyeon.devwebide.project.repository.ProjectRepository;
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
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(ProjectMemberService.class)
class ProjectMemberServiceTest {

    @Autowired
    private ProjectMemberService projectMemberService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private RuntimeRepository runtimeRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("프로젝트 멤버를 초대한다")
    void inviteMember() {
        TestProject testProject = createTestProject(ProjectStatus.ACTIVE);
        User invitedUser = saveUser("invited");
        ProjectMemberInviteRequest request = new ProjectMemberInviteRequest(
                invitedUser.getId(),
                ProjectMemberRole.EDITOR
        );

        ProjectMemberManageResponse response = projectMemberService.inviteMember(
                testProject.project().getId(),
                testProject.owner().getId(),
                request
        );

        ProjectMember projectMember = projectMemberRepository.findById(response.projectMemberId())
                .orElseThrow();

        assertThat(projectMember.getStatus()).isEqualTo(ProjectMemberStatus.INVITED);
        assertThat(projectMember.getRole()).isEqualTo(ProjectMemberRole.EDITOR);
        assertThat(projectMember.getInvitedByUser().getId()).isEqualTo(testProject.owner().getId());
        assertThat(projectMember.getInvitedAt()).isNotNull();
        assertThat(projectMember.getJoinedAt()).isNull();

        assertThat(response.userId()).isEqualTo(invitedUser.getId());
        assertThat(response.nickname()).isEqualTo(invitedUser.getNickname());
        assertThat(response.role()).isEqualTo(ProjectMemberRole.EDITOR);
        assertThat(response.status()).isEqualTo(ProjectMemberStatus.INVITED);
        assertThat(response.invitedAt()).isNotNull();
        assertThat(response.joinedAt()).isNull();
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트에는 멤버를 초대할 수 없다")
    void inviteMemberWithNotFoundProject() {
        User requester = saveUser("requester");
        User invitedUser = saveUser("invited");
        ProjectMemberInviteRequest request = new ProjectMemberInviteRequest(
                invitedUser.getId(),
                ProjectMemberRole.EDITOR
        );

        assertThatThrownBy(() -> projectMemberService.inviteMember(
                999999L,
                requester.getId(),
                request
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("ACTIVE가 아닌 프로젝트에는 멤버를 초대할 수 없다")
    void inviteMemberWithInactiveProject() {
        TestProject testProject = createTestProject(ProjectStatus.DELETED);
        User invitedUser = saveUser("invited");
        ProjectMemberInviteRequest request = new ProjectMemberInviteRequest(
                invitedUser.getId(),
                ProjectMemberRole.EDITOR
        );

        assertThatThrownBy(() -> projectMemberService.inviteMember(
                testProject.project().getId(),
                testProject.owner().getId(),
                request
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("OWNER가 아닌 요청자는 멤버를 초대할 수 없다")
    void inviteMemberByNonOwnerRequester() {
        TestProject testProject = createTestProject(ProjectStatus.ACTIVE);
        User requester = saveUser("requester");
        saveProjectMember(
                testProject.project(),
                requester,
                ProjectMemberRole.EDITOR,
                ProjectMemberStatus.ACTIVE
        );
        User invitedUser = saveUser("invited");
        ProjectMemberInviteRequest request = new ProjectMemberInviteRequest(
                invitedUser.getId(),
                ProjectMemberRole.EDITOR
        );

        assertThatThrownBy(() -> projectMemberService.inviteMember(
                testProject.project().getId(),
                requester.getId(),
                request
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("존재하지 않는 사용자는 초대할 수 없다")
    void inviteNotFoundUser() {
        TestProject testProject = createTestProject(ProjectStatus.ACTIVE);
        ProjectMemberInviteRequest request = new ProjectMemberInviteRequest(
                999999L,
                ProjectMemberRole.EDITOR
        );

        assertThatThrownBy(() -> projectMemberService.inviteMember(
                testProject.project().getId(),
                testProject.owner().getId(),
                request
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("OWNER 권한으로 초대할 수 없다")
    void inviteOwnerRole() {
        TestProject testProject = createTestProject(ProjectStatus.ACTIVE);
        User invitedUser = saveUser("invited");
        ProjectMemberInviteRequest request = new ProjectMemberInviteRequest(
                invitedUser.getId(),
                ProjectMemberRole.OWNER
        );

        assertThatThrownBy(() -> projectMemberService.inviteMember(
                testProject.project().getId(),
                testProject.owner().getId(),
                request
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("이미 ACTIVE 멤버인 사용자는 중복 초대할 수 없다")
    void inviteActiveMember() {
        TestProject testProject = createTestProject(ProjectStatus.ACTIVE);
        User invitedUser = saveUser("invited");
        saveProjectMember(
                testProject.project(),
                invitedUser,
                ProjectMemberRole.EDITOR,
                ProjectMemberStatus.ACTIVE
        );
        ProjectMemberInviteRequest request = new ProjectMemberInviteRequest(
                invitedUser.getId(),
                ProjectMemberRole.EDITOR
        );

        assertThatThrownBy(() -> projectMemberService.inviteMember(
                testProject.project().getId(),
                testProject.owner().getId(),
                request
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("이미 INVITED 멤버인 사용자는 중복 초대할 수 없다")
    void inviteInvitedMember() {
        TestProject testProject = createTestProject(ProjectStatus.ACTIVE);
        User invitedUser = saveUser("invited");
        saveProjectMember(
                testProject.project(),
                invitedUser,
                ProjectMemberRole.EDITOR,
                ProjectMemberStatus.INVITED
        );
        ProjectMemberInviteRequest request = new ProjectMemberInviteRequest(
                invitedUser.getId(),
                ProjectMemberRole.EDITOR
        );

        assertThatThrownBy(() -> projectMemberService.inviteMember(
                testProject.project().getId(),
                testProject.owner().getId(),
                request
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("REMOVED 멤버는 이번 MVP에서 재초대할 수 없다")
    void inviteRemovedMember() {
        TestProject testProject = createTestProject(ProjectStatus.ACTIVE);
        User invitedUser = saveUser("invited");
        saveProjectMember(
                testProject.project(),
                invitedUser,
                ProjectMemberRole.EDITOR,
                ProjectMemberStatus.REMOVED
        );
        ProjectMemberInviteRequest request = new ProjectMemberInviteRequest(
                invitedUser.getId(),
                ProjectMemberRole.EDITOR
        );

        assertThatThrownBy(() -> projectMemberService.inviteMember(
                testProject.project().getId(),
                testProject.owner().getId(),
                request
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("프로젝트 멤버 권한을 변경한다")
    void updateMemberRole() {
        TestProject testProject = createTestProject(ProjectStatus.ACTIVE);
        User memberUser = saveUser("member");
        ProjectMember member = saveProjectMember(
                testProject.project(),
                memberUser,
                ProjectMemberRole.EDITOR,
                ProjectMemberStatus.ACTIVE
        );
        ProjectMemberRoleUpdateRequest request =
                new ProjectMemberRoleUpdateRequest(ProjectMemberRole.VIEWER);

        ProjectMemberManageResponse response = projectMemberService.updateMemberRole(
                testProject.project().getId(),
                member.getId(),
                testProject.owner().getId(),
                request
        );

        assertThat(projectMemberRepository.findById(member.getId()).orElseThrow().getRole())
                .isEqualTo(ProjectMemberRole.VIEWER);
        assertThat(response.projectMemberId()).isEqualTo(member.getId());
        assertThat(response.role()).isEqualTo(ProjectMemberRole.VIEWER);
        assertThat(response.status()).isEqualTo(ProjectMemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("OWNER 권한은 변경할 수 없다")
    void updateOwnerRole() {
        TestProject testProject = createTestProject(ProjectStatus.ACTIVE);
        ProjectMemberRoleUpdateRequest request =
                new ProjectMemberRoleUpdateRequest(ProjectMemberRole.EDITOR);

        assertThatThrownBy(() -> projectMemberService.updateMemberRole(
                testProject.project().getId(),
                testProject.ownerMember().getId(),
                testProject.owner().getId(),
                request
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("OWNER 권한으로 변경할 수 없다")
    void updateMemberRoleToOwner() {
        TestProject testProject = createTestProject(ProjectStatus.ACTIVE);
        User memberUser = saveUser("member");
        ProjectMember member = saveProjectMember(
                testProject.project(),
                memberUser,
                ProjectMemberRole.EDITOR,
                ProjectMemberStatus.ACTIVE
        );
        ProjectMemberRoleUpdateRequest request =
                new ProjectMemberRoleUpdateRequest(ProjectMemberRole.OWNER);

        assertThatThrownBy(() -> projectMemberService.updateMemberRole(
                testProject.project().getId(),
                member.getId(),
                testProject.owner().getId(),
                request
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("프로젝트 멤버를 제거한다")
    void removeMember() {
        TestProject testProject = createTestProject(ProjectStatus.ACTIVE);
        User memberUser = saveUser("member");
        ProjectMember member = saveProjectMember(
                testProject.project(),
                memberUser,
                ProjectMemberRole.EDITOR,
                ProjectMemberStatus.ACTIVE
        );

        ProjectMemberManageResponse response = projectMemberService.removeMember(
                testProject.project().getId(),
                member.getId(),
                testProject.owner().getId()
        );

        assertThat(projectMemberRepository.findById(member.getId()).orElseThrow().getStatus())
                .isEqualTo(ProjectMemberStatus.REMOVED);
        assertThat(response.projectMemberId()).isEqualTo(member.getId());
        assertThat(response.status()).isEqualTo(ProjectMemberStatus.REMOVED);
    }

    @Test
    @DisplayName("OWNER 멤버는 제거할 수 없다")
    void removeOwner() {
        TestProject testProject = createTestProject(ProjectStatus.ACTIVE);

        assertThatThrownBy(() -> projectMemberService.removeMember(
                testProject.project().getId(),
                testProject.ownerMember().getId(),
                testProject.owner().getId()
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("프로젝트 멤버 초대를 수락한다")
    void acceptInvitation() {
        TestProject testProject = createTestProject(ProjectStatus.ACTIVE);
        User invitedUser = saveUser("invited");
        ProjectMember invitedMember = saveProjectMember(
                testProject.project(),
                invitedUser,
                ProjectMemberRole.EDITOR,
                ProjectMemberStatus.INVITED
        );

        ProjectMemberManageResponse response = projectMemberService.acceptInvitation(
                testProject.project().getId(),
                invitedMember.getId(),
                invitedUser.getId()
        );

        ProjectMember acceptedMember = projectMemberRepository.findById(invitedMember.getId())
                .orElseThrow();

        assertThat(acceptedMember.getStatus()).isEqualTo(ProjectMemberStatus.ACTIVE);
        assertThat(acceptedMember.getJoinedAt()).isNotNull();
        assertThat(response.projectMemberId()).isEqualTo(invitedMember.getId());
        assertThat(response.userId()).isEqualTo(invitedUser.getId());
        assertThat(response.role()).isEqualTo(ProjectMemberRole.EDITOR);
        assertThat(response.status()).isEqualTo(ProjectMemberStatus.ACTIVE);
        assertThat(response.invitedAt()).isNotNull();
        assertThat(response.joinedAt()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트의 초대는 수락할 수 없다")
    void acceptInvitationWithNotFoundProject() {
        User invitedUser = saveUser("invited");

        assertThatThrownBy(() -> projectMemberService.acceptInvitation(
                999999L,
                1L,
                invitedUser.getId()
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("ACTIVE가 아닌 프로젝트의 초대는 수락할 수 없다")
    void acceptInvitationWithInactiveProject() {
        TestProject testProject = createTestProject(ProjectStatus.DELETED);
        User invitedUser = saveUser("invited");
        ProjectMember invitedMember = saveProjectMember(
                testProject.project(),
                invitedUser,
                ProjectMemberRole.EDITOR,
                ProjectMemberStatus.INVITED
        );

        assertThatThrownBy(() -> projectMemberService.acceptInvitation(
                testProject.project().getId(),
                invitedMember.getId(),
                invitedUser.getId()
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("존재하지 않는 요청자는 초대를 수락할 수 없다")
    void acceptInvitationWithNotFoundRequester() {
        TestProject testProject = createTestProject(ProjectStatus.ACTIVE);
        User invitedUser = saveUser("invited");
        ProjectMember invitedMember = saveProjectMember(
                testProject.project(),
                invitedUser,
                ProjectMemberRole.EDITOR,
                ProjectMemberStatus.INVITED
        );

        assertThatThrownBy(() -> projectMemberService.acceptInvitation(
                testProject.project().getId(),
                invitedMember.getId(),
                999999L
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("존재하지 않는 멤버의 초대는 수락할 수 없다")
    void acceptInvitationWithNotFoundMember() {
        TestProject testProject = createTestProject(ProjectStatus.ACTIVE);
        User invitedUser = saveUser("invited");

        assertThatThrownBy(() -> projectMemberService.acceptInvitation(
                testProject.project().getId(),
                999999L,
                invitedUser.getId()
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("다른 프로젝트의 멤버 초대는 수락할 수 없다")
    void acceptInvitationWithOtherProjectMember() {
        TestProject testProject = createTestProject(ProjectStatus.ACTIVE);
        TestProject otherProject = createTestProject(ProjectStatus.ACTIVE);
        User invitedUser = saveUser("invited");
        ProjectMember invitedMember = saveProjectMember(
                otherProject.project(),
                invitedUser,
                ProjectMemberRole.EDITOR,
                ProjectMemberStatus.INVITED
        );

        assertThatThrownBy(() -> projectMemberService.acceptInvitation(
                testProject.project().getId(),
                invitedMember.getId(),
                invitedUser.getId()
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("초대 대상 사용자가 아니면 초대를 수락할 수 없다")
    void acceptInvitationByOtherUser() {
        TestProject testProject = createTestProject(ProjectStatus.ACTIVE);
        User invitedUser = saveUser("invited");
        User otherUser = saveUser("other");
        ProjectMember invitedMember = saveProjectMember(
                testProject.project(),
                invitedUser,
                ProjectMemberRole.EDITOR,
                ProjectMemberStatus.INVITED
        );

        assertThatThrownBy(() -> projectMemberService.acceptInvitation(
                testProject.project().getId(),
                invitedMember.getId(),
                otherUser.getId()
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("이미 ACTIVE 상태인 멤버는 초대를 수락할 수 없다")
    void acceptActiveMemberInvitation() {
        TestProject testProject = createTestProject(ProjectStatus.ACTIVE);
        User memberUser = saveUser("member");
        ProjectMember member = saveProjectMember(
                testProject.project(),
                memberUser,
                ProjectMemberRole.EDITOR,
                ProjectMemberStatus.ACTIVE
        );

        assertThatThrownBy(() -> projectMemberService.acceptInvitation(
                testProject.project().getId(),
                member.getId(),
                memberUser.getId()
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("REMOVED 상태인 멤버는 초대를 수락할 수 없다")
    void acceptRemovedMemberInvitation() {
        TestProject testProject = createTestProject(ProjectStatus.ACTIVE);
        User memberUser = saveUser("member");
        ProjectMember member = saveProjectMember(
                testProject.project(),
                memberUser,
                ProjectMemberRole.EDITOR,
                ProjectMemberStatus.REMOVED
        );

        assertThatThrownBy(() -> projectMemberService.acceptInvitation(
                testProject.project().getId(),
                member.getId(),
                memberUser.getId()
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("OWNER 멤버는 초대 수락 대상이 아니다")
    void acceptOwnerInvitation() {
        TestProject testProject = createTestProject(ProjectStatus.ACTIVE);

        assertThatThrownBy(() -> projectMemberService.acceptInvitation(
                testProject.project().getId(),
                testProject.ownerMember().getId(),
                testProject.owner().getId()
        )).isInstanceOf(IllegalArgumentException.class);
    }

    private TestProject createTestProject(ProjectStatus status) {
        User owner = saveUser("owner");
        Runtime runtime = runtimeRepository.save(createRuntime());
        Project project = projectRepository.save(Project.builder()
                .ownerUser(owner)
                .guestSession(null)
                .runtime(runtime)
                .name("team-project")
                .description("team project")
                .projectType(ProjectType.TEAM)
                .visibility(ProjectVisibility.TEAM)
                .status(status)
                .storagePath("/projects/" + UUID.randomUUID())
                .build());
        ProjectMember ownerMember = saveProjectMember(
                project,
                owner,
                ProjectMemberRole.OWNER,
                ProjectMemberStatus.ACTIVE
        );

        return new TestProject(project, owner, ownerMember);
    }

    private ProjectMember saveProjectMember(
            Project project,
            User user,
            ProjectMemberRole role,
            ProjectMemberStatus status
    ) {
        LocalDateTime now = LocalDateTime.now();

        return projectMemberRepository.save(ProjectMember.builder()
                .project(project)
                .user(user)
                .role(role)
                .status(status)
                .invitedByUser(null)
                .invitedAt(status == ProjectMemberStatus.INVITED ? now : null)
                .joinedAt(status == ProjectMemberStatus.ACTIVE ? now : null)
                .build());
    }

    private User saveUser(String nicknamePrefix) {
        return userRepository.save(User.builder()
                .email(nicknamePrefix + "-" + UUID.randomUUID() + "@example.com")
                .nickname(nicknamePrefix + "-" + UUID.randomUUID())
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build());
    }

    private Runtime createRuntime() {
        return Runtime.builder()
                .name("node-" + UUID.randomUUID())
                .displayName("Node.js")
                .version("20")
                .dockerImage("node:20")
                .language(RuntimeLanguage.NODE)
                .status(RuntimeStatus.ACTIVE)
                .build();
    }

    private record TestProject(
            Project project,
            User owner,
            ProjectMember ownerMember
    ) {
    }
}
