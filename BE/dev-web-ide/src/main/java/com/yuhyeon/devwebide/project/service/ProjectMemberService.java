package com.yuhyeon.devwebide.project.service;

import com.yuhyeon.devwebide.project.domain.*;
import com.yuhyeon.devwebide.project.dto.ProjectMemberInviteRequest;
import com.yuhyeon.devwebide.project.dto.ProjectMemberManageResponse;
import com.yuhyeon.devwebide.project.dto.ProjectMemberRoleUpdateRequest;
import com.yuhyeon.devwebide.project.repository.ProjectMemberRepository;
import com.yuhyeon.devwebide.project.repository.ProjectRepository;
import com.yuhyeon.devwebide.user.domain.User;
import com.yuhyeon.devwebide.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectMemberService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProjectMemberManageResponse inviteMember(
            Long projectId,
            Long requesterUserId,
            ProjectMemberInviteRequest request
    ) {
        Project project = getActiveProject(projectId);
        User requester = getUser(requesterUserId);

        validateOwnerRequester(projectId, requester.getId());
        validateManageableRole(request.role());

        User invitedUser = getUser(request.userId());

        projectMemberRepository.findByProjectIdAndUserId(projectId, invitedUser.getId())
                .ifPresent(projectMember -> {
                    throw new IllegalArgumentException("이미 프로젝트 멤버로 등록된 사용자입니다.");
                });

        ProjectMember projectMember = ProjectMember.builder()
                .project(project)
                .user(invitedUser)
                .role(request.role())
                .status(ProjectMemberStatus.INVITED)
                .invitedByUser(requester)
                .invitedAt(LocalDateTime.now())
                .joinedAt(null)
                .build();

        ProjectMember savedProjectMember = projectMemberRepository.save(projectMember);

        return ProjectMemberManageResponse.from(savedProjectMember);
    }

    @Transactional
    public ProjectMemberManageResponse updateMemberRole(
            Long projectId,
            Long memberId,
            Long requesterUserId,
            ProjectMemberRoleUpdateRequest request
    ) {
        getActiveProject(projectId);
        User requester = getUser(requesterUserId);

        validateOwnerRequester(projectId, requester.getId());
        validateManageableRole(request.role());

        ProjectMember projectMember = getManageableProjectMember(memberId, projectId);

        if (projectMember.isOwner()) {
            throw new IllegalArgumentException("OWNER 권한은 변경할 수 없습니다.");
        }

        projectMember.changeRole(request.role());

        return ProjectMemberManageResponse.from(projectMember);
    }

    @Transactional
    public ProjectMemberManageResponse removeMember(
            Long projectId,
            Long memberId,
            Long requesterUserId
    ) {
        getActiveProject(projectId);
        User requester = getUser(requesterUserId);

        validateOwnerRequester(projectId, requester.getId());

        ProjectMember projectMember = getManageableProjectMember(memberId, projectId);

        if (projectMember.isOwner()) {
            throw new IllegalArgumentException("OWNER 멤버는 제거할 수 없습니다.");
        }

        projectMember.remove();

        return ProjectMemberManageResponse.from(projectMember);
    }

    private Project getActiveProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));

        if (project.getStatus() != ProjectStatus.ACTIVE) {
            throw new IllegalArgumentException("ACTIVE 상태의 프로젝트만 멤버를 관리할 수 있습니다.");
        }

        return project;
    }

    private User getUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private void validateOwnerRequester(Long projectId, Long requesterUserId) {
        ProjectMember requesterMember = projectMemberRepository
                .findByProjectIdAndUserIdAndStatus(
                        projectId,
                        requesterUserId,
                        ProjectMemberStatus.ACTIVE
                )
                .orElseThrow(() -> new IllegalArgumentException("프로젝트 OWNER만 멤버를 관리할 수 있습니다."));

        if (!requesterMember.isOwner()) {
            throw new IllegalArgumentException("프로젝트 OWNER만 멤버를 관리할 수 있습니다.");
        }
    }

    private void validateManageableRole(ProjectMemberRole role) {
        if (role == null) {
            throw new IllegalArgumentException("프로젝트 멤버 권한은 필수입니다.");
        }

        if (role == ProjectMemberRole.OWNER) {
            throw new IllegalArgumentException("OWNER 권한은 멤버 관리 API에서 지정할 수 없습니다.");
        }
    }

    private ProjectMember getManageableProjectMember(Long memberId, Long projectId) {
        ProjectMember projectMember = projectMemberRepository
                .findByIdAndProjectId(memberId, projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트 멤버를 찾을 수 없습니다."));

        if (!projectMember.isActive() && !projectMember.isInvited()) {
            throw new IllegalArgumentException("ACTIVE 또는 INVITED 상태의 멤버만 관리할 수 있습니다.");
        }

        return projectMember;
    }
}
