package com.yuhyeon.devwebide.project.domain;

import com.yuhyeon.devwebide.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 프로젝트 멤버 엔티티
 *
 * 팀 프로젝트에 참여한 사용자와 권한을 관리합니다.
 * 프로젝트 생성자, 초대된 사용자, 권한 변경 대상 사용자를 저장합니다.
 */
@Entity
@Table(
        name = "project_members",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_project_member",
                        columnNames = {"project_id", "user_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectMember {

    /**
     * 프로젝트 멤버 PK
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 참여한 프로젝트
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /**
     * 프로젝트에 참여한 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 프로젝트 멤버 권한
     *
     * OWNER, MAINTAINER, EDITOR, VIEWER
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private ProjectMemberRole role;

    /**
     * 프로젝트 멤버 상태
     *
     * INVITED, ACTIVE, REMOVED
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ProjectMemberStatus status;

    /**
     * 초대한 사용자
     *
     * 프로젝트 생성자 OWNER 등록처럼 초대자가 없는 경우 null입니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_user_id")
    private User invitedByUser;

    /**
     * 초대 일시
     *
     * 초대 상태인 멤버에게 사용합니다.
     */
    @Column(name = "invited_at")
    private LocalDateTime invitedAt;

    /**
     * 참여 일시
     *
     * 초대를 수락했거나 프로젝트 생성자가 OWNER로 등록된 시점입니다.
     */
    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    /**
     * ProjectMember 생성자
     *
     * @param project 참여한 프로젝트
     * @param user 참여한 사용자
     * @param role 멤버 권한
     * @param status 멤버 상태
     * @param invitedByUser 초대한 사용자
     * @param invitedAt 초대 일시
     * @param joinedAt 참여 일시
     */
    @Builder
    public ProjectMember(
            Project project,
            User user,
            ProjectMemberRole role,
            ProjectMemberStatus status,
            User invitedByUser,
            LocalDateTime invitedAt,
            LocalDateTime joinedAt
    ) {
        validateRequiredFields(project, user, role, status);

        this.project = project;
        this.user = user;
        this.role = role;
        this.status = status;
        this.invitedByUser = invitedByUser;
        this.invitedAt = invitedAt;
        this.joinedAt = joinedAt;
    }

    /**
     * 필수 값 검증
     *
     * @param project 프로젝트
     * @param user 사용자
     * @param role 권한
     * @param status 상태
     */
    private void validateRequiredFields(
            Project project,
            User user,
            ProjectMemberRole role,
            ProjectMemberStatus status
    ) {
        if (project == null) {
            throw new IllegalArgumentException("프로젝트는 필수입니다.");
        }

        if (user == null) {
            throw new IllegalArgumentException("사용자는 필수입니다.");
        }

        if (role == null) {
            throw new IllegalArgumentException("프로젝트 멤버 권한은 필수입니다.");
        }

        if (status == null) {
            throw new IllegalArgumentException("프로젝트 멤버 상태는 필수입니다.");
        }
    }

    /**
     * 활성 멤버 여부 확인
     *
     * @return 활성 멤버 여부
     */
    public boolean isActive() {
        return this.status == ProjectMemberStatus.ACTIVE;
    }

    /**
     * 초대 상태 여부 확인
     *
     * @return 초대 상태 여부
     */
    public boolean isInvited() {
        return this.status == ProjectMemberStatus.INVITED;
    }

    /**
     * 제거된 멤버 여부 확인
     *
     * @return 제거 여부
     */
    public boolean isRemoved() {
        return this.status == ProjectMemberStatus.REMOVED;
    }

    /**
     * OWNER 권한 여부 확인
     *
     * @return OWNER 여부
     */
    public boolean isOwner() {
        return this.role == ProjectMemberRole.OWNER;
    }

    /**
     * 멤버 권한 변경
     *
     * @param role 변경할 권한
     */
    public void changeRole(ProjectMemberRole role) {
        if (role == null) {
            throw new IllegalArgumentException("변경할 권한은 필수입니다.");
        }

        this.role = role;
    }

    /**
     * 초대 수락 처리
     *
     * @param joinedAt 참여 일시
     */
    public void join(LocalDateTime joinedAt) {
        this.status = ProjectMemberStatus.ACTIVE;
        this.joinedAt = joinedAt;
    }

    public void reinvite(
            ProjectMemberRole role,
            User invitedByUser,
            LocalDateTime invitedAt
    ) {
        if (!isRemoved()) {
            throw new IllegalStateException("REMOVED 상태의 멤버만 재초대할 수 있습니다.");
        }

        if (role == null) {
            throw new IllegalArgumentException("재초대 권한은 필수입니다.");
        }

        this.role = role;
        this.status = ProjectMemberStatus.INVITED;
        this.invitedByUser = invitedByUser;
        this.invitedAt = invitedAt;
        this.joinedAt = null;
    }

    /**
     * 멤버 제거 처리
     *
     * 실제 DB row를 삭제하지 않고 상태만 REMOVED로 변경합니다.
     */
    public void remove() {
        this.status = ProjectMemberStatus.REMOVED;
    }
}
