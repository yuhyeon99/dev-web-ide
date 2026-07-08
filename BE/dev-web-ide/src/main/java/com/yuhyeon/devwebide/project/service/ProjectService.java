package com.yuhyeon.devwebide.project.service;

import com.yuhyeon.devwebide.project.domain.*;
import com.yuhyeon.devwebide.project.dto.*;
import com.yuhyeon.devwebide.project.repository.*;
import com.yuhyeon.devwebide.runtime.domain.Runtime;
import com.yuhyeon.devwebide.runtime.repository.RuntimeRepository;
import com.yuhyeon.devwebide.user.domain.GuestSession;
import com.yuhyeon.devwebide.user.domain.User;
import com.yuhyeon.devwebide.user.repository.GuestSessionRepository;
import com.yuhyeon.devwebide.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 프로젝트 서비스
 *
 * 프로젝트 생성, 조회, 수정, 삭제와 관련된 비즈니스 로직을 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional // 메서드 실행을 하나의 DB 트랜잭션으로 묶어줌
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectSettingsRepository projectSettingsRepository;
    private final ProjectFileRepository projectFileRepository;
    private final RuntimeRepository runtimeRepository;
    private final UserRepository userRepository;
    private final GuestSessionRepository guestSessionRepository;
    private final ProjectAccessLogRepository projectAccessLogRepository;

    /**
     * 프로젝트 생성
     *
     * 개인 프로젝트, 팀 프로젝트, 게스트 프로젝트를 생성합니다.
     *
     * ownerUserId는 회원 프로젝트 생성 시 사용하고,
     * guestSessionId는 게스트 프로젝트 생성 시 사용합니다.
     *
     * @param request 프로젝트 생성 요청
     * @param ownerUserId 현재 로그인한 회원 사용자 ID
     * @param guestSessionId 현재 게스트 세션 ID
     * @return 생성된 프로젝트 응답
     */
    public ProjectCreateResponse createProject(
            ProjectCreateRequest request,
            Long ownerUserId,
            Long guestSessionId
    ) {
        validateProjectCreateRequest(request);

        Runtime runtime = getActiveRuntime(request.runtimeId());

        Project project = createProjectEntity(
                request,
                runtime,
                ownerUserId,
                guestSessionId
        );

        Project savedProject = projectRepository.save(project);

        createProjectMembersIfTeamProject(
                savedProject,
                request,
                ownerUserId
        );

        createDefaultProjectSettings(savedProject);
        createRootProjectFile(savedProject);

        return ProjectCreateResponse.from(savedProject);
    }

    /**
     * 프로젝트 생성 요청 검증
     *
     * 프로젝트 타입과 공개 범위 조합을 검증합니다.
     *
     * @param request 프로젝트 생성 요청
     */
    private void validateProjectCreateRequest(ProjectCreateRequest request) {
        if (request.isTeamProject()
                && request.visibility() != ProjectVisibility.TEAM) {
            throw new IllegalArgumentException(
                    "팀 프로젝트의 공개 범위는 TEAM이어야 합니다."
            );
        }

        if (!request.isTeamProject()
                && request.visibility() != ProjectVisibility.PRIVATE) {
            throw new IllegalArgumentException(
                    "개인/게스트 프로젝트의 공개 범위는 PRIVATE이어야 합니다."
            );
        }
    }

    /**
     * 활성 런타임 조회
     *
     * @param runtimeId 런타임 ID
     * @return 활성 런타임
     */
    private Runtime getActiveRuntime(Long runtimeId) {
        Runtime runtime = runtimeRepository.findById(runtimeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 런타임입니다."
                ));

        if (!runtime.isActive()) {
            throw new IllegalArgumentException(
                    "비활성화된 런타임은 사용할 수 없습니다."
            );
        }

        return runtime;
    }

    /**
     * 프로젝트 엔티티 생성
     *
     * @param request 프로젝트 생성 요청
     * @param runtime 런타임
     * @param ownerUserId 회원 사용자 ID
     * @param guestSessionId 게스트 세션 ID
     * @return 프로젝트 엔티티
     */
    private Project createProjectEntity(
            ProjectCreateRequest request,
            Runtime runtime,
            Long ownerUserId,
            Long guestSessionId
    ) {
        if (request.isGuestProject()) {
            GuestSession guestSession = getGuestSession(guestSessionId);

            return Project.builder()
                    .ownerUser(null)
                    .guestSession(guestSession)
                    .runtime(runtime)
                    .name(request.name())
                    .description(request.description())
                    .projectType(ProjectType.GUEST)
                    .visibility(ProjectVisibility.PRIVATE)
                    .status(ProjectStatus.ACTIVE)
                    .storagePath(generateStoragePath())
                    .build();
        }

        User ownerUser = getUser(ownerUserId);

        return Project.builder()
                .ownerUser(ownerUser)
                .guestSession(null)
                .runtime(runtime)
                .name(request.name())
                .description(request.description())
                .projectType(request.projectType())
                .visibility(request.visibility())
                .status(ProjectStatus.ACTIVE)
                .storagePath(generateStoragePath())
                .build();
    }

    /**
     * 회원 사용자 조회
     *
     * @param userId 사용자 ID
     * @return 사용자
     */
    private User getUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "회원 프로젝트 생성에는 사용자 ID가 필요합니다."
            );
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 사용자입니다."
                ));
    }

    /**
     * 게스트 세션 조회
     *
     * @param guestSessionId 게스트 세션 ID
     * @return 게스트 세션
     */
    private GuestSession getGuestSession(Long guestSessionId) {
        if (guestSessionId == null) {
            throw new IllegalArgumentException(
                    "게스트 프로젝트 생성에는 게스트 세션 ID가 필요합니다."
            );
        }

        return guestSessionRepository.findById(guestSessionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 게스트 세션입니다."
                ));
    }

    /**
     * 팀 프로젝트 멤버 생성
     *
     * 팀 프로젝트인 경우 생성자를 OWNER로 등록하고,
     * memberUserIds에 포함된 사용자를 EDITOR 권한으로 초대합니다.
     *
     * @param project 생성된 프로젝트
     * @param request 프로젝트 생성 요청
     * @param ownerUserId 생성자 사용자 ID
     */
    private void createProjectMembersIfTeamProject(
            Project project,
            ProjectCreateRequest request,
            Long ownerUserId
    ) {
        if (!request.isTeamProject()) {
            return;
        }

        User ownerUser = getUser(ownerUserId);
        LocalDateTime now = LocalDateTime.now();

        ProjectMember ownerMember = ProjectMember.builder()
                .project(project)
                .user(ownerUser)
                .role(ProjectMemberRole.OWNER)
                .status(ProjectMemberStatus.ACTIVE)
                .invitedByUser(null)
                .invitedAt(null)
                .joinedAt(now)
                .build();

        projectMemberRepository.save(ownerMember);

        List<ProjectMember> invitedMembers =
                createInvitedMembers(project, request.memberUserIds(), ownerUser, now);

        projectMemberRepository.saveAll(invitedMembers);
    }

    /**
     * 초대 멤버 목록 생성
     *
     * 생성자 본인은 초대 대상에서 제외합니다.
     * 중복 사용자 ID는 제거합니다.
     *
     * @param project 프로젝트
     * @param memberUserIds 초대할 사용자 ID 목록
     * @param invitedByUser 초대한 사용자
     * @param invitedAt 초대 일시
     * @return 초대 멤버 목록
     */
    private List<ProjectMember> createInvitedMembers(
            Project project,
            List<Long> memberUserIds,
            User invitedByUser,
            LocalDateTime invitedAt
    ) {
        Set<Long> distinctMemberUserIds = new LinkedHashSet<>(memberUserIds);

        return distinctMemberUserIds.stream()
                .filter(memberUserId -> !memberUserId.equals(invitedByUser.getId()))
                .map(memberUserId -> createInvitedMember(
                        project,
                        memberUserId,
                        invitedByUser,
                        invitedAt
                ))
                .toList();
    }

    /**
     * 초대 멤버 생성
     *
     * @param project 프로젝트
     * @param memberUserId 초대할 사용자 ID
     * @param invitedByUser 초대한 사용자
     * @param invitedAt 초대 일시
     * @return 초대 멤버
     */
    private ProjectMember createInvitedMember(
            Project project,
            Long memberUserId,
            User invitedByUser,
            LocalDateTime invitedAt
    ) {
        User invitedUser = userRepository.findById(memberUserId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "초대 대상 사용자를 찾을 수 없습니다. userId=" + memberUserId
                ));

        return ProjectMember.builder()
                .project(project)
                .user(invitedUser)
                .role(ProjectMemberRole.EDITOR)
                .status(ProjectMemberStatus.INVITED)
                .invitedByUser(invitedByUser)
                .invitedAt(invitedAt)
                .joinedAt(null)
                .build();
    }

    /**
     * 기본 프로젝트 설정 생성
     *
     * @param project 프로젝트
     */
    private void createDefaultProjectSettings(Project project) {
        ProjectSettings projectSettings =
                ProjectSettings.createDefault(project);

        projectSettingsRepository.save(projectSettings);
    }

    /**
     * 루트 디렉토리 메타데이터 생성
     *
     * @param project 프로젝트
     */
    private void createRootProjectFile(Project project) {
        ProjectFile rootDirectory =
                ProjectFile.createRootDirectory(project);

        projectFileRepository.save(rootDirectory);
    }

    /**
     * 프로젝트 저장 경로 생성
     *
     * 현재는 실제 EFS 디렉토리 생성 전 단계이므로
     * UUID 기반 경로만 생성합니다.
     *
     * @return 프로젝트 저장 경로
     */
    private String generateStoragePath() {
        return "/projects/" + UUID.randomUUID();
    }

    /**
     * 내 프로젝트 목록 조회
     *
     * 로그인한 사용자가 생성한 ACTIVE 상태의 프로젝트 목록을 조회합니다.
     * 삭제 처리된 프로젝트는 조회하지 않습니다.
     *
     * @param ownerUserId 로그인한 사용자 ID
     * @return 내 프로젝트 목록
     */
    public List<ProjectSummaryResponse> getMyProjects(Long ownerUserId) {
        validateOwnerUserId(ownerUserId);

        List<Project> projects =
                projectRepository.findByOwnerUserIdAndStatusOrderByUpdatedAtDescIdDesc(
                        ownerUserId,
                        ProjectStatus.ACTIVE
                );

        return projects.stream()
                .map(ProjectSummaryResponse::from)
                .toList();
    }

    /**
     * 프로젝트 상세 조회
     *
     * ACTIVE 상태의 프로젝트만 조회합니다.
     * 프로젝트 기본 정보, 런타임, 설정, 활성 멤버 목록을 함께 반환합니다.
     *
     * @param projectId 프로젝트 ID
     * @return 프로젝트 상세 응답
     */
    @Transactional(readOnly = true)
    public ProjectDetailResponse getProjectDetail(Long projectId) {
        Project project = projectRepository.findByIdAndStatus(
                        projectId,
                        ProjectStatus.ACTIVE
                )
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않거나 삭제된 프로젝트입니다. projectId=" + projectId
                ));

        ProjectSettings projectSettings =
                projectSettingsRepository.findByProjectId(projectId)
                        .orElseThrow(() -> new IllegalStateException(
                                "프로젝트 설정이 존재하지 않습니다. projectId=" + projectId
                        ));

        List<ProjectMember> projectMembers =
                projectMemberRepository.findByProjectIdAndStatusOrderByJoinedAtDesc(
                        projectId,
                        ProjectMemberStatus.ACTIVE
                );

        return ProjectDetailResponse.from(
                project,
                projectSettings,
                projectMembers
        );
    }

    public ProjectDetailResponse updateProject(
            Long projectId,
            ProjectUpdateRequest request
    ) {
        Project project = projectRepository.findByIdAndStatus(
                        projectId,
                        ProjectStatus.ACTIVE
                )
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않거나 삭제된 프로젝트입니다. projectId=" + projectId
                ));

        project.rename(request.name());
        project.updateDescription(request.description());

        ProjectSettings projectSettings =
                projectSettingsRepository.findByProjectId(projectId)
                        .orElseThrow(() -> new IllegalStateException(
                                "프로젝트 설정이 존재하지 않습니다. projectId=" + projectId
                        ));

        List<ProjectMember> projectMembers =
                projectMemberRepository.findByProjectIdAndStatusOrderByJoinedAtDesc(
                        projectId,
                        ProjectMemberStatus.ACTIVE
                );

        return ProjectDetailResponse.from(
                project,
                projectSettings,
                projectMembers
        );
    }

    @Transactional
    public ProjectOpenResponse openProject(
            Long projectId,
            Long userId,
            Long guestSessionId
    ) {
        validateOpenRequester(userId, guestSessionId);

        Project project = projectRepository
                .findByIdAndStatus(projectId, ProjectStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException(
                        "프로젝트를 찾을 수 없습니다."
                ));

        User user = null;
        GuestSession guestSession = null;

        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "사용자를 찾을 수 없습니다."
                    ));
        }

        if (guestSessionId != null) {
            guestSession = guestSessionRepository.findById(guestSessionId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "게스트 세션을 찾을 수 없습니다."
                    ));
        }

        LocalDateTime openedAt = LocalDateTime.now();

        ProjectAccessLog accessLog = ProjectAccessLog.builder()
                .project(project)
                .user(user)
                .guestSession(guestSession)
                .accessType(ProjectAccessType.OPEN)
                .build();

        projectAccessLogRepository.save(accessLog);

        return ProjectOpenResponse.from(project, openedAt);
    }

    private void validateOpenRequester(
            Long userId,
            Long guestSessionId
    ) {
        boolean hasUserId = userId != null;
        boolean hasGuestSessionId = guestSessionId != null;

        if (hasUserId == hasGuestSessionId) {
            throw new IllegalArgumentException(
                    "회원 사용자 또는 게스트 세션 중 하나만 지정해야 합니다."
            );
        }
    }

    /**
     * 사용자 ID 필수 여부 검증
     *
     * @param ownerUserId 로그인한 사용자 ID
     */
    private void validateOwnerUserId(Long ownerUserId) {
        if (ownerUserId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
    }
}
