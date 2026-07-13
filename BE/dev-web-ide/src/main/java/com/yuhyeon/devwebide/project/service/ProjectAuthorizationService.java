package com.yuhyeon.devwebide.project.service;

import com.yuhyeon.devwebide.auth.dto.AuthenticatedPrincipal;
import com.yuhyeon.devwebide.project.domain.Project;
import com.yuhyeon.devwebide.project.domain.ProjectMemberStatus;
import com.yuhyeon.devwebide.project.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectAuthorizationService {

    private final ProjectMemberRepository projectMemberRepository;

    public Long requireUserPrincipal(AuthenticatedPrincipal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("인증 정보가 필요합니다.");
        }

        if (!principal.isUserSession()) {
            throw new IllegalArgumentException("회원 인증이 필요합니다.");
        }

        if (principal.userId() == null) {
            throw new IllegalArgumentException("사용자 ID가 필요합니다.");
        }

        return principal.userId();
    }

    public Long requireGuestPrincipal(AuthenticatedPrincipal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("인증 정보가 필요합니다.");
        }

        if (!principal.isGuestSession()) {
            throw new IllegalArgumentException("게스트 인증이 필요합니다.");
        }

        if (principal.guestSessionId() == null) {
            throw new IllegalArgumentException("게스트 세션 ID가 필요합니다.");
        }

        return principal.guestSessionId();
    }

    public void requireCanViewProject(Project project, Long userId) {
        if (project == null) {
            throw new IllegalArgumentException("프로젝트가 필요합니다.");
        }

        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID가 필요합니다.");
        }

        if (isOwner(project, userId) || isActiveMember(project.getId(), userId)) {
            return;
        }

        throw new IllegalArgumentException("프로젝트를 조회할 권한이 없습니다.");
    }

    public void requireCanViewGuestProject(Project project, Long guestSessionId) {
        if (project == null) {
            throw new IllegalArgumentException("프로젝트가 필요합니다.");
        }

        if (guestSessionId == null) {
            throw new IllegalArgumentException("게스트 세션 ID가 필요합니다.");
        }

        if (project.getGuestSession() != null
                && project.getGuestSession().getId().equals(guestSessionId)) {
            return;
        }

        throw new IllegalArgumentException("프로젝트를 조회할 권한이 없습니다.");
    }

    public void requireProjectOwner(Project project, Long userId) {
        if (project == null) {
            throw new IllegalArgumentException("프로젝트가 필요합니다.");
        }

        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID가 필요합니다.");
        }

        if (!isOwner(project, userId)) {
            throw new IllegalArgumentException("프로젝트 OWNER만 수행할 수 있습니다.");
        }
    }

    public void requireCanOpenProject(Project project, Long userId) {
        requireCanViewProject(project, userId);
    }

    public void requireCanOpenGuestProject(Project project, Long guestSessionId) {
        requireCanViewGuestProject(project, guestSessionId);
    }

    public boolean isOwner(Project project, Long userId) {
        return project.getOwnerUser() != null
                && project.getOwnerUser().getId().equals(userId);
    }

    public boolean isActiveMember(Long projectId, Long userId) {
        return projectMemberRepository
                .findByProjectIdAndUserIdAndStatus(
                        projectId,
                        userId,
                        ProjectMemberStatus.ACTIVE
                )
                .isPresent();
    }
}
