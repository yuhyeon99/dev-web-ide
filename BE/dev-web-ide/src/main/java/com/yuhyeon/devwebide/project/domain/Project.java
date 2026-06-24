package com.yuhyeon.devwebide.project.domain;

import com.yuhyeon.devwebide.runtime.domain.Runtime;
import com.yuhyeon.devwebide.user.domain.GuestSession;
import com.yuhyeon.devwebide.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 프로젝트 엔티티
 *
 * 웹 IDE에서 생성되는 개인 프로젝트, 팀 프로젝트, 게스트 프로젝트를 관리합니다.
 * 실제 프로젝트 파일 원본은 EFS에 저장하고,
 * DB에는 프로젝트 메타데이터와 저장 경로를 저장합니다.
 */
@Entity
@Table(name = "projects")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Project {

    /**
     * 프로젝트 PK
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 프로젝트 소유 회원
     *
     * 개인 프로젝트 또는 팀 프로젝트인 경우 값이 존재합니다.
     * 게스트 프로젝트인 경우 null입니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private User ownerUser;

    /**
     * 게스트 세션
     *
     * 게스트 프로젝트인 경우 값이 존재합니다.
     * 회원 프로젝트인 경우 null입니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_session_id")
    private GuestSession guestSession;

    /**
     * 프로젝트 런타임
     *
     * 프로젝트 생성 시 선택한 실행 환경입니다.
     * 예: Node.js, Python, Java, C++
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "runtime_id", nullable = false)
    private Runtime runtime;

    /**
     * 프로젝트 이름
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * 프로젝트 설명
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 프로젝트 타입
     *
     * PERSONAL, TEAM, GUEST
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "project_type", nullable = false, length = 30)
    private ProjectType projectType;

    /**
     * 프로젝트 공개 범위
     *
     * PRIVATE, TEAM
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 30)
    private ProjectVisibility visibility;

    /**
     * 프로젝트 상태
     *
     * ACTIVE, ARCHIVED, DELETED
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ProjectStatus status;

    /**
     * 프로젝트 파일 저장 경로
     *
     * 실제 파일은 DB가 아니라 EFS에 저장합니다.
     * 예: /projects/{projectId}
     */
    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    /**
     * 생성 일시
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 일시
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Project 생성자
     *
     * 개인/팀 프로젝트는 ownerUser가 필요하고,
     * 게스트 프로젝트는 guestSession이 필요합니다.
     *
     * @param ownerUser 프로젝트 소유 회원
     * @param guestSession 게스트 세션
     * @param runtime 프로젝트 런타임
     * @param name 프로젝트 이름
     * @param description 프로젝트 설명
     * @param projectType 프로젝트 타입
     * @param visibility 공개 범위
     * @param status 프로젝트 상태
     * @param storagePath 프로젝트 파일 저장 경로
     */
    @Builder
    public Project(
            User ownerUser,
            GuestSession guestSession,
            Runtime runtime,
            String name,
            String description,
            ProjectType projectType,
            ProjectVisibility visibility,
            ProjectStatus status,
            String storagePath
    ) {
        validateProjectOwner(projectType, ownerUser, guestSession);

        this.ownerUser = ownerUser;
        this.guestSession = guestSession;
        this.runtime = runtime;
        this.name = name;
        this.description = description;
        this.projectType = projectType;
        this.visibility = visibility;
        this.status = status;
        this.storagePath = storagePath;
    }

    /**
     * 프로젝트 타입에 맞는 소유자가 지정되었는지 검증합니다.
     *
     * @param projectType 프로젝트 타입
     * @param ownerUser 회원 소유자
     * @param guestSession 게스트 세션
     */
    private void validateProjectOwner(
            ProjectType projectType,
            User ownerUser,
            GuestSession guestSession
    ) {
        if (projectType == ProjectType.GUEST) {
            validateGuestProjectOwner(ownerUser, guestSession);
            return;
        }

        validateUserProjectOwner(ownerUser, guestSession);
    }

    /**
     * 게스트 프로젝트 소유자 검증
     *
     * 게스트 프로젝트는 guestSession만 가져야 합니다.
     *
     * @param ownerUser 회원 소유자
     * @param guestSession 게스트 세션
     */
    private void validateGuestProjectOwner(
            User ownerUser,
            GuestSession guestSession
    ) {
        if (ownerUser != null || guestSession == null) {
            throw new IllegalArgumentException(
                    "게스트 프로젝트는 guestSession만 지정해야 합니다."
            );
        }
    }

    /**
     * 회원 프로젝트 소유자 검증
     *
     * 개인/팀 프로젝트는 ownerUser만 가져야 합니다.
     *
     * @param ownerUser 회원 소유자
     * @param guestSession 게스트 세션
     */
    private void validateUserProjectOwner(
            User ownerUser,
            GuestSession guestSession
    ) {
        if (ownerUser == null || guestSession != null) {
            throw new IllegalArgumentException(
                    "개인/팀 프로젝트는 ownerUser만 지정해야 합니다."
            );
        }
    }

    /**
     * 개인 프로젝트 여부 확인
     *
     * @return 개인 프로젝트 여부
     */
    public boolean isPersonalProject() {
        return this.projectType == ProjectType.PERSONAL;
    }

    /**
     * 팀 프로젝트 여부 확인
     *
     * @return 팀 프로젝트 여부
     */
    public boolean isTeamProject() {
        return this.projectType == ProjectType.TEAM;
    }

    /**
     * 게스트 프로젝트 여부 확인
     *
     * @return 게스트 프로젝트 여부
     */
    public boolean isGuestProject() {
        return this.projectType == ProjectType.GUEST;
    }

    /**
     * 활성 프로젝트 여부 확인
     *
     * @return 활성 여부
     */
    public boolean isActive() {
        return this.status == ProjectStatus.ACTIVE;
    }

    /**
     * 프로젝트 이름 변경
     *
     * @param name 변경할 프로젝트 이름
     */
    public void rename(String name) {
        this.name = name;
    }

    /**
     * 프로젝트 설명 변경
     *
     * @param description 변경할 프로젝트 설명
     */
    public void updateDescription(String description) {
        this.description = description;
    }

    /**
     * 프로젝트 보관 처리
     */
    public void archive() {
        this.status = ProjectStatus.ARCHIVED;
    }

    /**
     * 프로젝트 삭제 처리
     *
     * 실제 DB row를 삭제하지 않고 상태만 DELETED로 변경합니다.
     */
    public void delete() {
        this.status = ProjectStatus.DELETED;
    }
}